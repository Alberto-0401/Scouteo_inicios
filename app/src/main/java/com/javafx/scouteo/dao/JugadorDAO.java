package com.javafx.scouteo.dao;

import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.util.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.Period;

public class JugadorDAO {

    public List<Jugador> obtenerTodos() {
        List<Jugador> jugadores = new ArrayList<>();
        String sql = "SELECT * FROM jugadores";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Jugador jugador = new Jugador();
                jugador.setId(rs.getInt("id_jugador"));
                jugador.setNombre(rs.getString("nombre"));
                jugador.setApellidos(rs.getString("apellidos"));
                jugador.setDorsal(rs.getInt("dorsal"));
                jugador.setPosicion(rs.getString("posicion"));
                jugador.setCategoria(rs.getString("categoria"));

                // Calcular edad desde fecha_nacimiento
                Date fechaNac = rs.getDate("fecha_nacimiento");
                if (fechaNac != null) {
                    LocalDate fechaNacimiento = fechaNac.toLocalDate();
                    jugador.setEdad(Period.between(fechaNacimiento, LocalDate.now()).getYears());
                }

                jugadores.add(jugador);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jugadores;
    }

    public Jugador obtenerPorId(int id) {
        String sql = "SELECT * FROM jugadores WHERE id_jugador = ?";
        Jugador jugador = null;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                jugador = new Jugador();
                jugador.setId(rs.getInt("id_jugador"));
                jugador.setNombre(rs.getString("nombre"));
                jugador.setApellidos(rs.getString("apellidos"));
                jugador.setDorsal(rs.getInt("dorsal"));
                jugador.setPosicion(rs.getString("posicion"));
                jugador.setCategoria(rs.getString("categoria"));

                Date fechaNac = rs.getDate("fecha_nacimiento");
                if (fechaNac != null) {
                    LocalDate fechaNacimiento = fechaNac.toLocalDate();
                    jugador.setEdad(Period.between(fechaNacimiento, LocalDate.now()).getYears());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jugador;
    }

    public boolean insertar(Jugador jugador, Date fechaNacimiento) {
        String sql = "INSERT INTO jugadores (nombre, apellidos, fecha_nacimiento, dorsal, posicion, categoria, altura, peso, foto) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jugador.getNombre());
            pstmt.setString(2, jugador.getApellidos());
            pstmt.setDate(3, fechaNacimiento);
            pstmt.setInt(4, jugador.getDorsal());
            pstmt.setString(5, jugador.getPosicion());
            pstmt.setString(6, jugador.getCategoria());
            pstmt.setNull(7, Types.DOUBLE); // altura
            pstmt.setNull(8, Types.DOUBLE); // peso
            pstmt.setNull(9, Types.VARCHAR); // foto

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Jugador jugador, Date fechaNacimiento) {
        String sql = "UPDATE jugadores SET nombre = ?, apellidos = ?, fecha_nacimiento = ?, dorsal = ?, posicion = ?, categoria = ? WHERE id_jugador = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jugador.getNombre());
            pstmt.setString(2, jugador.getApellidos());
            pstmt.setDate(3, fechaNacimiento);
            pstmt.setInt(4, jugador.getDorsal());
            pstmt.setString(5, jugador.getPosicion());
            pstmt.setString(6, jugador.getCategoria());
            pstmt.setInt(7, jugador.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM jugadores WHERE id_jugador = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int contarTotal() {
        String sql = "SELECT COUNT(*) as total FROM jugadores";
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
