package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.JugadorPartido;
import com.javafx.scouteo.util.ConexionBD;

import java.sql.*;

public class JugadorPartidoDAO {

    public boolean insertar(JugadorPartido jugadorPartido) {
        String sql = "INSERT INTO jugadores_partidos (id_jugador, id_partido, id_estadistica, titular, convocado) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jugadorPartido.getIdJugador());
            pstmt.setInt(2, jugadorPartido.getIdPartido());
            pstmt.setInt(3, jugadorPartido.getIdEstadistica());
            pstmt.setBoolean(4, jugadorPartido.getTitular());
            pstmt.setBoolean(5, jugadorPartido.getConvocado());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
