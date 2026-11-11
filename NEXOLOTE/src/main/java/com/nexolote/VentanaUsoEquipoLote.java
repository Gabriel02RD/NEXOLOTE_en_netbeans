package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

public class VentanaUsoEquipoLote extends JFrame {
    private final JComboBox<Estilos.Opcion> cbLote = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbEquipo = new JComboBox<>();
    private final JTextArea txtObs = new JTextArea(3, 20);
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Lote", "Equipo", "Tipo", "Observaciones"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idLote = -1, idEquipo = -1;

    public VentanaUsoEquipoLote() {
        setTitle("NEXOLOTE | Uso de equipos por lote"); setSize(1120, 720); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Uso de equipos por lote", "Equipos utilizados durante la producción."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Registro de uso");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Lote de producción *", cbLote); Estilos.agregarCampo(form, gbc, 2, 0, "Equipo *", cbEquipo);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.insets = new java.awt.Insets(5, 5, 5, 8);
        form.add(Estilos.crearEtiqueta("Observaciones"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1; gbc.insets = new java.awt.Insets(5, 5, 5, 14);
        Estilos.estilizarAreaTexto(txtObs); form.add(new JScrollPane(txtObs), gbc);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Registrar"), bActualizar = Estilos.botonActualizar("Modificar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Registros de uso");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargarCombos(); cargar();
    }

    private void cargarCombos() { cargarOp(cbLote, "SELECT id_lote, codigo_lote FROM LOTE_PRODUCCION ORDER BY codigo_lote"); cargarOp(cbEquipo, "SELECT id_equipo, CONCAT(codigo_inventario,' - ',nombre) FROM EQUIPO ORDER BY nombre"); }
    private void cargarOp(JComboBox<Estilos.Opcion> cb, String sql) { cb.removeAllItems(); try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT l.codigo_lote, e.codigo_inventario, e.nombre, e.tipo_equipo, u.observaciones, u.id_lote, u.id_equipo FROM USO_EQUIPO_LOTE u INNER JOIN LOTE_PRODUCCION l ON u.id_lote=l.id_lote INNER JOIN EQUIPO e ON u.id_equipo=e.id_equipo ORDER BY l.codigo_lote"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getString(1), rs.getString(2) + " - " + rs.getString(3), rs.getString(4), rs.getString(5)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() { if (cbLote.getSelectedItem() == null || cbEquipo.getSelectedItem() == null) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO USO_EQUIPO_LOTE (id_lote, id_equipo, observaciones) VALUES (?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void actualizar() { if (idLote == -1 || idEquipo == -1) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE USO_EQUIPO_LOTE SET id_lote=?, id_equipo=?, observaciones=? WHERE id_lote=? AND id_equipo=?")) { llenar(ps); ps.setInt(4, idLote); ps.setInt(5, idEquipo); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void llenar(PreparedStatement ps) throws SQLException { ps.setInt(1, Estilos.idDe(cbLote)); ps.setInt(2, Estilos.idDe(cbEquipo)); ps.setString(3, txtObs.getText().trim()); }
    private void eliminar() { if (idLote == -1 || idEquipo == -1) return; if (JOptionPane.showConfirmDialog(this, "¿Eliminar la relación?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM USO_EQUIPO_LOTE WHERE id_lote=? AND id_equipo=?")) { ps.setInt(1, idLote); ps.setInt(2, idEquipo); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT u.id_lote, u.id_equipo, u.observaciones FROM USO_EQUIPO_LOTE u INNER JOIN LOTE_PRODUCCION l ON u.id_lote=l.id_lote INNER JOIN EQUIPO e ON u.id_equipo=e.id_equipo WHERE l.codigo_lote=? AND CONCAT(e.codigo_inventario,' - ',e.nombre)=?")) { ps.setString(1, modelo.getValueAt(f, 0).toString()); ps.setString(2, modelo.getValueAt(f, 1).toString()); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { idLote = rs.getInt(1); idEquipo = rs.getInt(2); seleccionarOp(cbLote, idLote); seleccionarOp(cbEquipo, idEquipo); txtObs.setText(rs.getString(3) == null ? "" : rs.getString(3)); } } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }
    private void seleccionarOp(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idLote = -1; idEquipo = -1; txtObs.setText(""); tabla.clearSelection(); }
}