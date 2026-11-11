package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class VentanaIncidencias extends JFrame {
    private final JComboBox<Estilos.Opcion> cbLote = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbResp = new JComboBox<>();
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Calidad", "Producción", "Equipo", "Materia prima", "Almacenamiento", "Otro"});
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Abierta", "En proceso", "Resuelta", "Cerrada"});
    private final JTextArea txtDesc = new JTextArea(4, 20);
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Lote", "Responsable", "Fecha", "Tipo", "Descripción", "Estado"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaIncidencias() {
        setTitle("NEXOLOTE | Incidencias"); setSize(1220, 740); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Incidencias", "Registro y seguimiento de eventos."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Datos de la incidencia");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Lote de producción *", cbLote); Estilos.agregarCampo(form, gbc, 2, 0, "Responsable", cbResp);
        Estilos.agregarCampo(form, gbc, 0, 1, "Tipo *", cbTipo); Estilos.agregarCampo(form, gbc, 2, 1, "Estado", cbEstado);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.insets = new java.awt.Insets(5, 5, 5, 8);
        form.add(Estilos.crearEtiqueta("Descripción *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1; gbc.insets = new java.awt.Insets(5, 5, 5, 14);
        Estilos.estilizarAreaTexto(txtDesc); form.add(new JScrollPane(txtDesc), gbc);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Registrar"), bActualizar = Estilos.botonActualizar("Modificar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Historial de incidencias");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargarCombos(); cargar();
    }

    private void cargarCombos() {
        cargarOp(cbLote, "SELECT id_lote, codigo_lote FROM LOTE_PRODUCCION ORDER BY codigo_lote");
        cbResp.removeAllItems(); cbResp.addItem(new Estilos.Opcion(-1, "Sin asignar"));
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT id_responsable, CONCAT(nombres,' ',apellidos) FROM RESPONSABLE WHERE estado='Activo' ORDER BY nombres"); ResultSet rs = ps.executeQuery()) { while (rs.next()) cbResp.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }
    private void cargarOp(JComboBox<Estilos.Opcion> cb, String sql) { cb.removeAllItems(); try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT i.id_incidencia, l.codigo_lote, r.nombres, r.apellidos, i.fecha_registro, i.tipo_incidencia, i.descripcion, i.estado FROM INCIDENCIA i INNER JOIN LOTE_PRODUCCION l ON i.id_lote=l.id_lote LEFT JOIN RESPONSABLE r ON i.id_responsable=r.id_responsable ORDER BY i.fecha_registro DESC"); ResultSet rs = ps.executeQuery()) { while (rs.next()) { String resp = rs.getString("nombres"); resp = (resp == null) ? "Sin asignar" : resp + " " + rs.getString("apellidos"); modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), resp, rs.getTimestamp(5), rs.getString(6), rs.getString(7), rs.getString(8)}); } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() { if (cbLote.getSelectedItem() == null || txtDesc.getText().trim().isEmpty()) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO INCIDENCIA (id_lote, id_responsable, fecha_registro, tipo_incidencia, descripcion, estado) VALUES (?,?,?,?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void actualizar() { if (idSeleccionado == -1) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE INCIDENCIA SET id_lote=?, id_responsable=?, tipo_incidencia=?, descripcion=?, estado=? WHERE id_incidencia=?")) { ps.setInt(1, Estilos.idDe(cbLote)); Estilos.Opcion r = (Estilos.Opcion) cbResp.getSelectedItem(); if (r == null || r.id == -1) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, r.id); ps.setString(3, cbTipo.getSelectedItem().toString()); ps.setString(4, txtDesc.getText().trim()); ps.setString(5, cbEstado.getSelectedItem().toString()); ps.setInt(6, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void llenar(PreparedStatement ps) throws SQLException {
        ps.setInt(1, Estilos.idDe(cbLote)); Estilos.Opcion r = (Estilos.Opcion) cbResp.getSelectedItem(); if (r == null || r.id == -1) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, r.id);
        ps.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now())); ps.setString(4, cbTipo.getSelectedItem().toString()); ps.setString(5, txtDesc.getText().trim()); ps.setString(6, cbEstado.getSelectedItem().toString());
    }

    private void eliminar() { if (idSeleccionado == -1) return; if (JOptionPane.showConfirmDialog(this, "¿Eliminar la incidencia?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM INCIDENCIA WHERE id_incidencia=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT * FROM INCIDENCIA WHERE id_incidencia=?")) { ps.setInt(1, idSeleccionado); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { seleccionarOp(cbLote, rs.getInt("id_lote")); int r = rs.getInt("id_responsable"); seleccionarOp(cbResp, rs.wasNull() ? -1 : r); cbTipo.setSelectedItem(rs.getString("tipo_incidencia")); txtDesc.setText(rs.getString("descripcion") == null ? "" : rs.getString("descripcion")); cbEstado.setSelectedItem(rs.getString("estado")); } } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }
    private void seleccionarOp(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idSeleccionado = -1; txtDesc.setText(""); tabla.clearSelection(); }
}