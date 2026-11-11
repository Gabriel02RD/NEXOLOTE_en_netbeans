package com.nexolote;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class VentanaPrincipal extends JFrame {
    private final JPanel panelContenido;
    private final CardLayout tarjetas;
    private JButton botonActivo;
    private JPanel stripKPIs;

    public VentanaPrincipal() {
        setTitle("NEXOLOTE · Sistema de gestión"); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 840); setMinimumSize(new Dimension(1100, 700)); setLocationRelativeTo(null);
        Estilos.configurarVentana(this);

        tarjetas = new CardLayout(); panelContenido = new JPanel(tarjetas); panelContenido.setBackground(Estilos.FONDO);
        add(crearSidebar(), BorderLayout.WEST); add(panelContenido, BorderLayout.CENTER);
        panelContenido.add(crearPanelInicio(), "Inicio"); tarjetas.show(panelContenido, "Inicio");
    }

    private JPanel crearSidebar() {
        JPanel lateral = new JPanel(new BorderLayout()); lateral.setPreferredSize(new Dimension(248, 0)); lateral.setBackground(Estilos.AZUL_PETROLEO);

        JPanel marca = new JPanel(); marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS)); marca.setBackground(Estilos.AZUL_PETROLEO);
        marca.setBorder(BorderFactory.createEmptyBorder(26, 22, 22, 18));
        JLabel nombre = new JLabel("NEXOLOTE"); nombre.setFont(new Font(Estilos.FAMILIA, Font.BOLD, 22)); nombre.setForeground(Color.WHITE); nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Sistema de gestión"); sub.setFont(Estilos.PEQUENA); sub.setForeground(new Color(180, 205, 210)); sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        marca.add(nombre); marca.add(Box.createVerticalStrut(3)); marca.add(sub); lateral.add(marca, BorderLayout.NORTH);

        JPanel menu = new JPanel(); menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS)); menu.setBackground(Estilos.AZUL_PETROLEO); menu.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        menu.add(crearBotonMenu("Inicio", true));
        menu.add(crearSeccion("Consultas"));
        menu.add(crearBotonMenu("Trazabilidad", false));
        menu.add(crearSeccion("Catálogos"));
        menu.add(crearBotonMenu("Productos", false)); menu.add(crearBotonMenu("Materias primas", false)); menu.add(crearBotonMenu("Proveedores", false));
        menu.add(crearBotonMenu("Responsables", false)); menu.add(crearBotonMenu("Ubicaciones", false));
        menu.add(crearSeccion("Lotes"));
        menu.add(crearBotonMenu("Lotes de materia prima", false)); menu.add(crearBotonMenu("Lotes de producción", false));
        menu.add(crearSeccion("Operaciones"));
        menu.add(crearBotonMenu("Equipos", false)); menu.add(crearBotonMenu("Uso de equipos por lote", false)); menu.add(crearBotonMenu("Mantenimiento", false));
        menu.add(crearBotonMenu("Consumo de materiales", false)); menu.add(crearBotonMenu("Movimientos de lotes", false));
        menu.add(crearSeccion("Sistema")); menu.add(crearBotonMenu("Incidencias", false));

        JScrollPane scroll = new JScrollPane(menu); scroll.setBorder(null); scroll.getViewport().setBackground(Estilos.AZUL_PETROLEO); lateral.add(scroll, BorderLayout.CENTER);
        JLabel pie = new JLabel("v3.1 · Trazabilidad", SwingConstants.CENTER); pie.setFont(Estilos.PEQUENA); pie.setForeground(new Color(180, 205, 210)); pie.setBorder(BorderFactory.createEmptyBorder(12, 5, 18, 5));
        lateral.add(pie, BorderLayout.SOUTH); return lateral;
    }

    private JLabel crearSeccion(String texto) {
        JLabel l = new JLabel(texto.toUpperCase()); l.setFont(new Font(Estilos.FAMILIA, Font.BOLD, 10)); l.setForeground(new Color(140, 170, 178));
        l.setBorder(BorderFactory.createEmptyBorder(14, 12, 6, 8)); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }

    private JButton crearBotonMenu(String nombre, boolean activo) {
        JButton b = new JButton(nombre); b.setFont(Estilos.NORMAL); b.setForeground(activo ? Color.WHITE : new Color(200, 220, 224));
        b.setBackground(activo ? Estilos.AZUL_TABLA : Estilos.AZUL_PETROLEO); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true); b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 8));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (b != botonActivo) b.setBackground(Estilos.AZUL_TABLA); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { if (b != botonActivo) b.setBackground(Estilos.AZUL_PETROLEO); }
        });

        if (activo) botonActivo = b;
        b.addActionListener(e -> { activar(b); if ("Inicio".equals(nombre)) { recargarKPIs(); tarjetas.show(panelContenido, "Inicio"); } else abrirModulo(nombre); });
        return b;
    }

    private void activar(JButton b) {
        if (botonActivo != null) { botonActivo.setBackground(Estilos.AZUL_PETROLEO); botonActivo.setForeground(new Color(200, 220, 224)); }
        b.setBackground(Estilos.AZUL_TABLA); b.setForeground(Color.WHITE); botonActivo = b;
    }

    private JPanel crearPanelInicio() {
        JPanel inicio = new JPanel(new BorderLayout(0, 20)); inicio.setBackground(Estilos.FONDO); inicio.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        JPanel bienvenida = new JPanel(new BorderLayout(0, 6)); bienvenida.setBackground(Estilos.SUPERFICIE);
        bienvenida.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        JLabel t = new JLabel("Panel de control"); t.setFont(new Font(Estilos.FAMILIA, Font.BOLD, 20)); t.setForeground(Estilos.AZUL_PETROLEO);
        JLabel s = new JLabel("Sistema de gestión de inventario y trazabilidad de lotes."); s.setFont(Estilos.SUBTITULO); s.setForeground(Estilos.TEXTO_SEC);
        bienvenida.add(t, BorderLayout.NORTH); bienvenida.add(s, BorderLayout.CENTER); inicio.add(bienvenida, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(); cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS)); cuerpo.setOpaque(false);
        JLabel lblResumen = new JLabel("Resumen del sistema"); lblResumen.setFont(Estilos.SECCION); lblResumen.setForeground(Estilos.TEXTO); lblResumen.setAlignmentX(Component.LEFT_ALIGNMENT);
        stripKPIs = new JPanel(new GridLayout(1, 4, 12, 12)); stripKPIs.setOpaque(false); stripKPIs.setAlignmentX(Component.LEFT_ALIGNMENT);
        cargarKPIsIniciales();
        cuerpo.add(lblResumen); cuerpo.add(Box.createVerticalStrut(10)); cuerpo.add(stripKPIs); cuerpo.add(Box.createVerticalStrut(24));

        JLabel lblModulos = new JLabel("Módulos del sistema"); lblModulos.setFont(Estilos.SECCION); lblModulos.setForeground(Estilos.TEXTO); lblModulos.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel gridModulos = new JPanel(new GridLayout(0, 3, 14, 14)); gridModulos.setOpaque(false); gridModulos.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Trazabilidad y los 13 módulos de las tablas:
        agregarTarjeta(gridModulos, "Trazabilidad", "Origen, recorrido e historial de cada lote", "Trazabilidad");
        agregarTarjeta(gridModulos, "Productos", "Catálogo de productos terminados", "Productos");
        agregarTarjeta(gridModulos, "Materias primas", "Insumos disponibles para producción", "Materias primas");
        agregarTarjeta(gridModulos, "Proveedores", "Registro y control de proveedores", "Proveedores");
        
        agregarTarjeta(gridModulos, "Responsables", "Personas encargadas de los procesos", "Responsables");
        agregarTarjeta(gridModulos, "Ubicaciones", "Espacios físicos de almacenamiento", "Ubicaciones");
        agregarTarjeta(gridModulos, "Lotes de materia prima", "Recepción y trazabilidad de insumos", "Lotes de materia prima");
        
        agregarTarjeta(gridModulos, "Lotes de producción", "Seguimiento de la fabricación", "Lotes de producción");
        agregarTarjeta(gridModulos, "Equipos", "Inventario de maquinaria y equipos", "Equipos");
        agregarTarjeta(gridModulos, "Uso de equipos por lote", "Asignación de maquinaria a lotes", "Uso de equipos por lote");
        
        agregarTarjeta(gridModulos, "Mantenimiento", "Programación de mantenimientos", "Mantenimiento");
        agregarTarjeta(gridModulos, "Consumo de materiales", "Insumos usados en la fabricación", "Consumo de materiales");
        agregarTarjeta(gridModulos, "Movimientos de lotes", "Traslados entre ubicaciones", "Movimientos de lotes");
        
        agregarTarjeta(gridModulos, "Incidencias", "Registro de eventos y alertas", "Incidencias");

        cuerpo.add(lblModulos); cuerpo.add(Box.createVerticalStrut(10)); cuerpo.add(gridModulos);
        JScrollPane scroll = new JScrollPane(cuerpo); scroll.setBorder(null); scroll.getViewport().setBackground(Estilos.FONDO); inicio.add(scroll, BorderLayout.CENTER);
        return inicio;
    }

    private void cargarKPIsIniciales() {
        stripKPIs.removeAll();
        stripKPIs.add(Estilos.crearKPI("Productos", "...", Estilos.TURQUESA)); stripKPIs.add(Estilos.crearKPI("Lotes MP", "...", Estilos.AZUL_INFO));
        stripKPIs.add(Estilos.crearKPI("Lotes prod.", "...", Estilos.AZUL_TABLA)); stripKPIs.add(Estilos.crearKPI("Incidencias", "...", Estilos.ROJO));
        stripKPIs.revalidate(); stripKPIs.repaint(); recargarKPIs();
    }

    private void recargarKPIs() {
        if (stripKPIs == null) return;
        new javax.swing.SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() { return new Object[]{ contar("PRODUCTO"), contar("LOTE_MATERIA_PRIMA"), contar("LOTE_PRODUCCION"), contar("INCIDENCIA", "estado NOT IN ('Cerrada','Resuelta')") }; }
            @Override protected void done() {
                try {
                    Object[] v = get(); stripKPIs.removeAll();
                    stripKPIs.add(Estilos.crearKPI("Productos", String.valueOf(v[0]), Estilos.TURQUESA)); stripKPIs.add(Estilos.crearKPI("Lotes MP", String.valueOf(v[1]), Estilos.AZUL_INFO));
                    stripKPIs.add(Estilos.crearKPI("Lotes prod.", String.valueOf(v[2]), Estilos.AZUL_TABLA)); stripKPIs.add(Estilos.crearKPI("Incidencias", String.valueOf(v[3]), Estilos.ROJO));
                    stripKPIs.revalidate(); stripKPIs.repaint();
                } catch (Exception ignored) { }
            }
        }.execute();
    }

    private int contar(String tabla) { return contar(tabla, null); }
    private int contar(String tabla, String where) {
        String sql = "SELECT COUNT(*) FROM " + tabla + (where != null ? " WHERE " + where : "");
        try (Connection con = Conexion.conectar()) { if (con == null) return 0; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; } } catch (SQLException e) { return 0; }
    }

    private void agregarTarjeta(JPanel panel, String nombre, String descripcion, String modulo) {
        JPanel card = new JPanel(new BorderLayout(0, 8)); card.setBackground(Estilos.SUPERFICIE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel titulo = new JLabel(nombre); titulo.setFont(Estilos.NEGRITA); titulo.setForeground(Estilos.AZUL_PETROLEO);
        JLabel detalle = new JLabel("<html><div style='width:220px'>" + descripcion + "</div></html>"); detalle.setFont(Estilos.SUBTITULO); detalle.setForeground(Estilos.TEXTO_SEC);
        JButton btn = Estilos.botonSecundario("Abrir módulo"); btn.setPreferredSize(new Dimension(0, 32)); btn.addActionListener(e -> abrirModulo(modulo));
        JPanel contenido = new JPanel(new BorderLayout(0, 8)); contenido.setOpaque(false); contenido.add(detalle, BorderLayout.NORTH); contenido.add(btn, BorderLayout.SOUTH);
        card.add(titulo, BorderLayout.NORTH); card.add(contenido, BorderLayout.CENTER); panel.add(card);
    }

    private void abrirModulo(String nombre) {
        switch (nombre) {
            case "Trazabilidad" -> new VentanaTrazabilidad().setVisible(true);
            case "Productos" -> new VentanaProductos().setVisible(true);
            case "Materias primas" -> new VentanaMateriasPrimas().setVisible(true);
            case "Proveedores" -> new VentanaProveedores().setVisible(true);
            case "Responsables" -> new VentanaResponsables().setVisible(true);
            case "Ubicaciones" -> new VentanaUbicaciones().setVisible(true);
            case "Lotes de materia prima" -> new VentanaLotesMateriaPrima().setVisible(true);
            case "Lotes de producción" -> new VentanaLotesProduccion().setVisible(true);
            case "Equipos" -> new VentanaEquipos().setVisible(true);
            case "Uso de equipos por lote" -> new VentanaUsoEquipoLote().setVisible(true);
            case "Mantenimiento" -> new VentanaMantenimientos().setVisible(true);
            case "Consumo de materiales" -> new VentanaConsumoMateriaPrima().setVisible(true);
            case "Movimientos de lotes" -> new VentanaMovimientosLote().setVisible(true);
            case "Incidencias" -> new VentanaIncidencias().setVisible(true);
            default -> JOptionPane.showMessageDialog(this, "Módulo no disponible", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
}