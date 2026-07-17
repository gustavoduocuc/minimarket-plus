package com.minimarket.repository;

import com.minimarket.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioUsernameOrderByFechaDesc(String username);
}
