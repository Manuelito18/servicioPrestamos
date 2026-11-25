package com.fernandez_terrones.prestamoservice.service;

import com.fernandez_terrones.prestamoservice.dto.PrestamoRequest;
import com.fernandez_terrones.prestamoservice.dto.PrestamoResponse;
import com.fernandez_terrones.prestamoservice.dto.ResumenResponse;
import com.fernandez_terrones.prestamoservice.entity.Prestamo;
import com.fernandez_terrones.prestamoservice.entity.Usuario;
import com.fernandez_terrones.prestamoservice.exception.ResourceNotFoundException;
import com.fernandez_terrones.prestamoservice.repository.PrestamoRepository;
import com.fernandez_terrones.prestamoservice.repository.UsuarioRepository;
import com.fernandez_terrones.prestamoservice.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final int ESCALA = 2;

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listar() {
        Usuario usuario = usuarioAutenticado();
        return prestamoRepository.findAllByUsuarioIdOrderByIdDesc(usuario.getId())
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long id) {
        return aRespuesta(buscarDelUsuarioAutenticado(id));
    }

    @Transactional
    public PrestamoResponse crear(PrestamoRequest request) {
        Usuario usuario = usuarioAutenticado();

        Prestamo prestamo = new Prestamo();
        aplicarDatos(prestamo, request);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setPagado(Boolean.TRUE.equals(request.pagado()));
        prestamo.setUsuario(usuario);

        return aRespuesta(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse actualizar(Long id, PrestamoRequest request) {
        Prestamo prestamo = buscarDelUsuarioAutenticado(id);

        aplicarDatos(prestamo, request);
        if (request.pagado() != null) {
            prestamo.setPagado(request.pagado());
        }

        return aRespuesta(prestamoRepository.save(prestamo));
    }

    @Transactional
    public void eliminar(Long id) {
        prestamoRepository.delete(buscarDelUsuarioAutenticado(id));
    }

    /**
     * Resumen de la cartera del prestamista autenticado:
     * capitalPrestado = suma de montos,
     * totalPorCobrar = suma de (monto + monto * interes / 100),
     * gananciaEsperada = totalPorCobrar - capitalPrestado.
     */
    @Transactional(readOnly = true)
    public ResumenResponse resumen() {
        Usuario usuario = usuarioAutenticado();
        List<Prestamo> prestamos = prestamoRepository.findAllByUsuarioIdOrderByIdDesc(usuario.getId());

        BigDecimal capital = BigDecimal.ZERO;
        BigDecimal totalPorCobrar = BigDecimal.ZERO;

        for (Prestamo prestamo : prestamos) {
            capital = capital.add(prestamo.getMonto());
            totalPorCobrar = totalPorCobrar.add(
                    calcularTotalCobrar(prestamo.getMonto(), prestamo.getInteres()));
        }

        capital = capital.setScale(ESCALA, RoundingMode.HALF_UP);
        totalPorCobrar = totalPorCobrar.setScale(ESCALA, RoundingMode.HALF_UP);
        BigDecimal ganancia = totalPorCobrar.subtract(capital).setScale(ESCALA, RoundingMode.HALF_UP);

        return new ResumenResponse(capital, totalPorCobrar, ganancia);
    }

    /**
     * Total a cobrar = monto + (monto * interes / 100).
     */
    public static BigDecimal calcularTotalCobrar(BigDecimal monto, BigDecimal interes) {
        BigDecimal ganancia = monto.multiply(interes)
                .divide(CIEN, ESCALA, RoundingMode.HALF_UP);
        return monto.add(ganancia).setScale(ESCALA, RoundingMode.HALF_UP);
    }

    private Usuario usuarioAutenticado() {
        String email = SecurityUtils.emailUsuarioAutenticado();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario autenticado ya no existe"));
    }

    private Prestamo buscarDelUsuarioAutenticado(Long id) {
        Usuario usuario = usuarioAutenticado();
        return prestamoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prestamo no encontrado con id " + id));
    }

    private void aplicarDatos(Prestamo prestamo, PrestamoRequest request) {
        prestamo.setCliente(request.cliente().trim());
        prestamo.setDni(request.dni());
        prestamo.setMonto(request.monto().setScale(ESCALA, RoundingMode.HALF_UP));
        prestamo.setInteres(request.interes().setScale(ESCALA, RoundingMode.HALF_UP));
        prestamo.setFechaVencimiento(request.fechaVencimiento());
    }

    private PrestamoResponse aRespuesta(Prestamo prestamo) {
        BigDecimal monto = prestamo.getMonto().setScale(ESCALA, RoundingMode.HALF_UP);
        BigDecimal interes = prestamo.getInteres().setScale(ESCALA, RoundingMode.HALF_UP);

        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getCliente(),
                prestamo.getDni(),
                monto,
                interes,
                calcularTotalCobrar(monto, interes),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(),
                prestamo.isPagado()
        );
    }
}
