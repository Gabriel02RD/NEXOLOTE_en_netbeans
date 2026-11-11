package com.nexolote;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout; // <-- ¡ESTE ERA EL QUE FALTABA!
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class Estilos {

    // Paleta de colores
    public static final Color AZUL_PETROLEO  = new Color(22, 58, 67);
    public static final Color AZUL_TABLA     = new Color(28, 79, 92);
    public static final Color AZUL_OSCURO    = new Color(15, 42, 49);
    public static final Color TURQUESA       = new Color(22, 138, 131);
    public static final Color TURQUESA_HOVER = new Color(18, 118, 112);
    public static final Color AZUL_INFO      = new Color(48, 111, 168);
    public static final Color AZUL_INFO_HOVER= new Color(40, 95, 145);
    public static final Color ROJO           = new Color(200, 60, 60);
    public static final Color ROJO_HOVER     = new Color(178, 48, 48);
    public static final Color GRIS_BOTON     = new Color(95, 105, 112);
    public static final Color GRIS_BOTON_HOVER = new Color(78, 87, 94);
    
    public static final Color FONDO          = new Color(241, 244, 247);
    public static final Color SUPERFICIE     = Color.WHITE;
    public static final Color BORDE          = new Color(212, 220, 226);
    public static final Color TEXTO          = new Color(35, 45, 52);
    public static final Color TEXTO_SEC      = new Color(95, 110, 120);
    public static final Color SELECCION      = new Color(210, 235, 235);

    // Fuentes
    public static final String FAMILIA = "Segoe UI";
    public static final Font TITULO      = new Font(FAMILIA, Font.BOLD, 22);
    public static final Font SECCION     = new Font(FAMILIA, Font.BOLD, 15);
    public static final Font SUBTITULO   = new Font(FAMILIA, Font.PLAIN, 13);
    public static final Font NORMAL      = new Font(FAMILIA, Font.PLAIN, 13);
    public static final Font NEGRITA     = new Font(FAMILIA, Font.BOLD, 13);
    public static final Font PEQUENA     = new Font(FAMILIA, Font.PLAIN, 11);
    public static final Font BOTON_FUENTE= new Font(FAMILIA, Font.BOLD, 12);
    public static final Font KPI_VALOR   = new Font(FAMILIA, Font.BOLD, 24);
    public static final Font KPI_LABEL   = new Font(FAMILIA, Font.PLAIN, 11);

    public static final int ALTO_CAMPO = 34;
    public static final int ALTO_BOTON = 36;

    private Estilos() { }

    // Clase para botones con hover
    public static class Boton extends JButton {
        private final Color base, hover;
        public Boton(String texto, Color base, Color hover) {
            super(texto);
            this.base = base; this.hover = hover;
            setFont(BOTON_FUENTE); setForeground(Color.WHITE); setBackground(base);
            setOpaque(true); setBorderPainted(false); setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setPreferredSize(new Dimension(getPreferredSize().width, ALTO_BOTON));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { setBackground(Boton.this.hover); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { setBackground(Boton.this.base); }
            });
        }
    }

    public static JButton botonGuardar(String t)    { return new Boton(t, TURQUESA, TURQUESA_HOVER); }
    public static JButton botonActualizar(String t) { return new Boton(t, AZUL_INFO, AZUL_INFO_HOVER); }
    public static JButton botonEliminar(String t)   { return new Boton(t, ROJO, ROJO_HOVER); }
    public static JButton botonLimpiar(String t)    { return new Boton(t, GRIS_BOTON, GRIS_BOTON_HOVER); }
    public static JButton botonSecundario(String t) { return new Boton(t, AZUL_PETROLEO, AZUL_OSCURO); }

    // Métodos de compatibilidad
    public static void estilizarBoton(JButton b) {
        b.setFont(BOTON_FUENTE); b.setBackground(TURQUESA); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setOpaque(true); b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, ALTO_BOTON));
    }
    public static void estilizarBotonEliminar(JButton b) { estilizarBoton(b); b.setBackground(ROJO); }
    public static void estilizarBotonSecundario(JButton b) { estilizarBoton(b); b.setBackground(AZUL_PETROLEO); }
    public static void estilizarBotonAzul(JButton b) { estilizarBoton(b); b.setBackground(AZUL_INFO); }

    public static void aplicarEstiloVentana(JFrame f) {
        f.getContentPane().setBackground(FONDO);
        f.setLayout(new BorderLayout());
    }
    public static void configurarVentana(JFrame f) { aplicarEstiloVentana(f); }

    public static JLabel crearTitulo(String texto) { JLabel l = new JLabel(texto); l.setFont(TITULO); l.setForeground(AZUL_PETROLEO); return l; }
    public static JLabel crearSubtitulo(String texto) { JLabel l = new JLabel(texto); l.setFont(SUBTITULO); l.setForeground(TEXTO_SEC); return l; }
    public static JLabel crearEtiqueta(String texto) { JLabel l = new JLabel(texto); l.setFont(NEGRITA); l.setForeground(TEXTO); return l; }

    public static JPanel crearEncabezado(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(18, 26, 18, 26)
        ));
        p.add(crearTitulo(titulo), BorderLayout.NORTH);
        p.add(crearSubtitulo(subtitulo), BorderLayout.CENTER);
        return p;
    }

    public static JPanel crearPanelTarjeta() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        return p;
    }

    public static JPanel crearPanelFormulario(String titulo) {
        JPanel p = crearPanelTarjeta();
        if (titulo != null && !titulo.isEmpty()) {
            JLabel l = new JLabel(titulo);
            l.setFont(SECCION); l.setForeground(AZUL_PETROLEO);
            l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));
            l.setPreferredSize(new Dimension(0, 30));
            p.add(l, BorderLayout.NORTH);
        }
        return p;
    }

    public static JPanel crearPanelTabla(String titulo) {
        JPanel p = crearPanelTarjeta();
        if (titulo != null && !titulo.isEmpty()) {
            JLabel l = new JLabel(titulo);
            l.setFont(SECCION); l.setForeground(TEXTO);
            p.add(l, BorderLayout.NORTH);
        }
        return p;
    }

    public static JPanel crearBarraBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        return p;
    }

    public static JPanel crearContenedorFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    public static void estilizarCampo(JTextField f) {
        f.setFont(NORMAL); f.setForeground(TEXTO); f.setBackground(SUPERFICIE);
        f.setCaretColor(TURQUESA); f.setSelectionColor(SELECCION);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setPreferredSize(new Dimension(180, ALTO_CAMPO));
    }
    public static void estilizarCombo(JComboBox<?> c) {
        c.setFont(NORMAL); c.setForeground(TEXTO); c.setBackground(SUPERFICIE);
        c.setPreferredSize(new Dimension(180, ALTO_CAMPO));
    }
    public static void estilizarAreaTexto(JTextArea a) {
        a.setFont(NORMAL); a.setForeground(TEXTO); a.setBackground(SUPERFICIE);
        a.setCaretColor(TURQUESA); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    public static void agregarCampo(JPanel p, GridBagConstraints gbc, int col, int fila, String etiqueta, JComponent campo) {
        gbc.gridx = col; gbc.gridy = fila; gbc.gridwidth = 1; gbc.gridheight = 1;
        gbc.weightx = 0; gbc.weighty = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 8);
        p.add(crearEtiqueta(etiqueta), gbc);

        if (campo instanceof JTextField) estilizarCampo((JTextField) campo);
        else if (campo instanceof JComboBox) estilizarCombo((JComboBox<?>) campo);
        else if (campo instanceof JTextArea) estilizarAreaTexto((JTextArea) campo);

        gbc.gridx = col + 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 5, 5, 14);
        p.add(campo, gbc);
    }

    public static void agregarAyuda(JPanel p, GridBagConstraints gbc, int fila, int columnas, String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(PEQUENA); l.setForeground(TEXTO_SEC);
        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = columnas; gbc.gridheight = 1;
        gbc.weightx = 1; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 5, 0, 5);
        p.add(l, gbc);
    }

    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(NORMAL); tabla.setForeground(TEXTO); tabla.setBackground(SUPERFICIE);
        tabla.setSelectionBackground(SELECCION); tabla.setSelectionForeground(TEXTO);
        tabla.setRowHeight(32); tabla.setGridColor(BORDE);
        tabla.setShowVerticalLines(true); tabla.setShowHorizontalLines(true);
        tabla.setIntercellSpacing(new Dimension(1, 1)); tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = tabla.getTableHeader();
        header.setOpaque(true); header.setBackground(AZUL_TABLA); header.setForeground(Color.WHITE);
        header.setFont(NEGRITA); header.setReorderingAllowed(false); header.setPreferredSize(new Dimension(0, 38));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(AZUL_TABLA); c.setForeground(Color.WHITE); c.setFont(NEGRITA);
                setHorizontalAlignment(JLabel.CENTER); setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return c;
            }
        };
        header.setDefaultRenderer(headerRenderer);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    public static JScrollPane crearScroll(JTable tabla) {
        JScrollPane s = new JScrollPane(tabla);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        s.getViewport().setBackground(SUPERFICIE);
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }

    public static JPanel crearKPI(String etiqueta, String valor, Color acento) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        JPanel barra = new JPanel(); barra.setBackground(acento); barra.setPreferredSize(new Dimension(4, 0));
        p.add(barra, BorderLayout.WEST);
        JPanel info = new JPanel(new BorderLayout(0, 2)); info.setOpaque(false); info.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel l = new JLabel(etiqueta.toUpperCase()); l.setFont(KPI_LABEL); l.setForeground(TEXTO_SEC);
        JLabel v = new JLabel(valor); v.setFont(KPI_VALOR); v.setForeground(TEXTO);
        info.add(l, BorderLayout.NORTH); info.add(v, BorderLayout.CENTER); p.add(info, BorderLayout.CENTER);
        return p;
    }


    // ------------------------------------------------------------------
    // Validaciones y utilidades comunes de los formularios
    // ------------------------------------------------------------------

    /** Error de validación con un mensaje pensado para mostrarse tal cual al usuario. */
    public static class ValidacionException extends RuntimeException {
        public ValidacionException(String mensaje) { super(mensaje); }
    }

    /** Ejecuta una acción de un botón y muestra mensajes claros si algo falla. */
    public static void ejecutar(java.awt.Component padre, Runnable accion) {
        try {
            accion.run();
        } catch (ValidacionException e) {
            javax.swing.JOptionPane.showMessageDialog(padre, e.getMessage(), "Revisa los datos", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(padre, "No se pudo completar la acción. Revisa que las fechas tengan el formato AAAA-MM-DD\ny que los números usen punto decimal.", "Revisa los datos", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    public static java.sql.Date fechaObligatoria(String texto, String campo) {
        java.sql.Date d = fechaOpcional(texto, campo);
        if (d == null) throw new ValidacionException(campo + " es obligatoria (formato AAAA-MM-DD).");
        return d;
    }

    public static java.sql.Date fechaOpcional(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            return java.sql.Date.valueOf(java.time.LocalDate.parse(texto.trim()));
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidacionException(campo + " no es una fecha válida. Usa el formato AAAA-MM-DD, por ejemplo 2026-10-28.");
        }
    }

    public static String textoFecha(java.sql.Date d) { return d == null ? "" : d.toString(); }

    public static java.math.BigDecimal decimal(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) throw new ValidacionException(campo + " es obligatorio.");
        try {
            return new java.math.BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ValidacionException(campo + " debe ser un número, por ejemplo 12.5.");
        }
    }

    /** Devuelve el id de la opción elegida en un combo o avisa que falta elegir. */
    public static int idDe(JComboBox<Opcion> combo) {
        Opcion o = (Opcion) combo.getSelectedItem();
        if (o == null) throw new ValidacionException("Selecciona una opción en todas las listas obligatorias.");
        return o.id;
    }

    /** Reglas de movimientos: Ingreso (sin origen), Traslado (origen y destino distintos), Salida (sin destino). */
    public static void validarMovimiento(String tipo, int origen, int destino) {
        boolean sinOrigen = origen == -1, sinDestino = destino == -1;
        switch (tipo) {
            case "Ingreso":
                if (!sinOrigen || sinDestino) throw new ValidacionException("Un Ingreso no tiene origen y necesita una ubicación de destino.");
                break;
            case "Traslado":
                if (sinOrigen || sinDestino) throw new ValidacionException("Un Traslado necesita ubicación de origen y de destino.");
                if (origen == destino) throw new ValidacionException("El origen y el destino de un Traslado deben ser distintos.");
                break;
            case "Salida":
                if (sinOrigen || !sinDestino) throw new ValidacionException("Una Salida necesita ubicación de origen y no tiene destino.");
                break;
            default:
                throw new ValidacionException("Tipo de movimiento no válido.");
        }
    }

    public static class Opcion {
        public final int id; public final String nombre;
        public Opcion(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}