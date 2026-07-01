package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.util.ValidationUtil;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

/**
 * ProjectDialog - Formulario de Projeto
 *
 * Melhorias de Usabilidade:
 * - Seletor de data com JSpinner (dia/mes/ano separados) sem digitacao manual
 * - Preview das datas de fases calculadas em tempo real antes de salvar
 * - Erros exibidos inline, proximos ao campo, sem popups bloqueantes
 * - Layout mais organizado com secoes claras
 * - Validacao de campos obrigatorios e numericos (orcamento)
 */
public class ProjectDialog extends JDialog {

    private final JTextField txtName   = new JTextField(22);
    private final JTextField txtDesc   = new JTextField(22);
    private final JComboBox<Enums.Priority> cbPriority = new JComboBox<>(Enums.Priority.values());
    private final JTextField txtBudget = new JTextField(10);

    private final JSpinner spnStartDay   = new JSpinner(new SpinnerNumberModel(LocalDate.now().getDayOfMonth(), 1, 31, 1));
    private final JSpinner spnStartMonth = new JSpinner(new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1));
    private final JSpinner spnStartYear  = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
    private final JSpinner spnEndDay     = new JSpinner(new SpinnerNumberModel(LocalDate.now().getDayOfMonth(), 1, 31, 1));
    private final JSpinner spnEndMonth   = new JSpinner(new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1));
    private final JSpinner spnEndYear    = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear() + 1, 2000, 2100, 1));

    private final JLabel lblPhaseReq  = new JLabel("—");
    private final JLabel lblPhaseDev  = new JLabel("—");
    private final JLabel lblPhaseDep  = new JLabel("—");

    private final JLabel lblErrName   = buildErrorLabel();
    private final JLabel lblErrBudget = buildErrorLabel();
    private final JLabel lblErrDate   = buildErrorLabel();

    private boolean saved = false;
    private Project project;

    public ProjectDialog(Frame owner, Project project) {
        super(owner, project == null ? "Novo Projeto" : "Editar Projeto", true);
        this.project = project;
        setSize(540, 600);
        setResizable(false);
        setLocationRelativeTo(owner);
        buildUI();
        if (project != null) populateFields();
        updatePhasePreview();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel lblTitle = new JLabel(project == null ? "Novo Projeto" : "Editar Projeto");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(16, 22, 10, 22));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 1, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int r = 0;

        // Nome
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("Nome do Projeto *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; form.add(txtName, gbc); r++;
        gbc.gridx = 1; gbc.gridy = r++; gbc.gridwidth = 3; form.add(lblErrName, gbc);
        txtName.getDocument().addDocumentListener(new DocListener(() ->
            lblErrName.setText(ValidationUtil.isNotEmpty(txtName.getText()) ? "" : "O nome e obrigatorio.")));

        // Descricao
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("Descricao"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; form.add(txtDesc, gbc); r++;

        // Prioridade + Orcamento
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("Prioridade"), gbc);
        gbc.gridx = 1; form.add(cbPriority, gbc);
        gbc.gridx = 2; form.add(lbl("Orcamento (R$) *"), gbc);
        gbc.gridx = 3; form.add(txtBudget, gbc); r++;
        gbc.gridx = 2; gbc.gridy = r++; gbc.gridwidth = 2; form.add(lblErrBudget, gbc);
        txtBudget.getDocument().addDocumentListener(new DocListener(() -> {
            if (!ValidationUtil.isNotEmpty(txtBudget.getText())) lblErrBudget.setText("O orcamento e obrigatorio.");
            else if (!ValidationUtil.isNumeric(txtBudget.getText())) lblErrBudget.setText("Informe um numero valido.");
            else if (Double.parseDouble(txtBudget.getText().replace(",", ".")) < 0) lblErrBudget.setText("Nao pode ser negativo.");
            else lblErrBudget.setText("");
        }));

        // Secao datas
        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 4; form.add(sectionTitle("Periodo do Projeto"), gbc);

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("Data de Inicio *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; form.add(dateRow(spnStartDay, spnStartMonth, spnStartYear), gbc); r++;

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("Data de Termino *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; form.add(dateRow(spnEndDay, spnEndMonth, spnEndYear), gbc); r++;

        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 4; form.add(lblErrDate, gbc);

        javax.swing.event.ChangeListener dl = e -> updatePhasePreview();
        spnStartDay.addChangeListener(dl); spnStartMonth.addChangeListener(dl); spnStartYear.addChangeListener(dl);
        spnEndDay.addChangeListener(dl);   spnEndMonth.addChangeListener(dl);   spnEndYear.addChangeListener(dl);

        // Preview fases
        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 4;
        form.add(sectionTitle("Preview das Fases (calculado automaticamente)"), gbc);

        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 4;
        form.add(buildPhasePreview(), gbc);

        // Rodape
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setBackground(new Color(241, 245, 249));
        btnCancel.setForeground(new Color(30, 41, 59));
        btnCancel.setBorderPainted(false); btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Salvar Projeto");
        btnSave.setBackground(new Color(59, 130, 246));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSave.setBorderPainted(false); btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> save());

        footer.add(btnCancel);
        footer.add(btnSave);

        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(form, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildPhasePreview() {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setBackground(Color.WHITE);
        p.add(phaseBlock("Requisitos (20%)", lblPhaseReq, new Color(139, 92, 246)));
        p.add(phaseBlock("Desenvolvimento (60%)", lblPhaseDev, new Color(59, 130, 246)));
        p.add(phaseBlock("Implantacao (20%)", lblPhaseDep, new Color(16, 185, 129)));
        return p;
    }

    private JPanel phaseBlock(String title, JLabel lbl, Color color) {
        JPanel b = new JPanel(new BorderLayout(0, 3));
        b.setBackground(new Color(248, 250, 252));
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        JPanel bar = new JPanel(); bar.setBackground(color); bar.setPreferredSize(new Dimension(0, 3));
        JLabel t = new JLabel(title); t.setFont(new Font("SansSerif", Font.BOLD, 10)); t.setForeground(new Color(30,41,59));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11)); lbl.setForeground(new Color(100,116,139));
        b.add(bar, BorderLayout.NORTH); b.add(t, BorderLayout.CENTER); b.add(lbl, BorderLayout.SOUTH);
        return b;
    }

    private void updatePhasePreview() {
        try {
            LocalDate s = getDate(spnStartDay, spnStartMonth, spnStartYear);
            LocalDate e = getDate(spnEndDay, spnEndMonth, spnEndYear);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (e.isAfter(s)) {
                long tot = ChronoUnit.DAYS.between(s, e);
                long rD = Math.round(tot * 0.20), dD = Math.round(tot * 0.60);
                lblPhaseReq.setText("ate " + s.plusDays(rD).format(dtf));
                lblPhaseDev.setText("ate " + s.plusDays(rD + dD).format(dtf));
                lblPhaseDep.setText("ate " + e.format(dtf));
                lblErrDate.setText("");
            } else {
                lblPhaseReq.setText("—"); lblPhaseDev.setText("—"); lblPhaseDep.setText("—");
                if (!s.equals(e)) lblErrDate.setText("Termino deve ser posterior ao inicio.");
            }
        } catch (Exception ex) {
            lblPhaseReq.setText("—"); lblPhaseDev.setText("—"); lblPhaseDep.setText("—");
            lblErrDate.setText("Data invalida.");
        }
    }

    private void save() {
        lblErrName.setText(""); lblErrBudget.setText(""); lblErrDate.setText("");
        boolean ok = true;

        // Validacao do Nome
        if (!ValidationUtil.isNotEmpty(txtName.getText())) { lblErrName.setText("O nome e obrigatorio."); ok = false; }
        else if (!ValidationUtil.hasMaxLength(txtName.getText(), 100)) { lblErrName.setText("Maximo 100 caracteres."); ok = false; }

        // Validacao do Orcamento
        double budget = 0;
        if (!ValidationUtil.isNotEmpty(txtBudget.getText())) { lblErrBudget.setText("O orcamento e obrigatorio."); ok = false; }
        else if (!ValidationUtil.isNumeric(txtBudget.getText())) { lblErrBudget.setText("Informe um numero valido."); ok = false; }
        else {
            budget = Double.parseDouble(txtBudget.getText().replace(",", "."));
            if (budget < 0) { lblErrBudget.setText("Nao pode ser negativo."); ok = false; }
        }

        // Validacao das Datas
        LocalDate start = null, end = null;
        try {
            start = getDate(spnStartDay, spnStartMonth, spnStartYear);
            end   = getDate(spnEndDay, spnEndMonth, spnEndYear);
            if (!end.isAfter(start)) { lblErrDate.setText("Termino deve ser posterior ao inicio."); ok = false; }
        } catch (Exception ex) { lblErrDate.setText("Data invalida."); ok = false; }

        if (!ok) return;

        if (project == null) project = new Project();
        project.setName(txtName.getText().trim());
        project.setDescription(txtDesc.getText().trim());
        project.setStartDate(start);
        project.setDeadline(end);
        project.setBudget(budget);
        project.setPriority((Enums.Priority) cbPriority.getSelectedItem());

        long tot = ChronoUnit.DAYS.between(start, end);
        long rD = Math.round(tot * 0.20), dD = Math.round(tot * 0.60);
        project.setRequirementsDeadline(start.plusDays(rD));
        project.setDevelopmentDeadline(start.plusDays(rD + dD));
        project.setDeploymentDeadline(end);

        saved = true;
        dispose();
    }

    private void populateFields() {
        txtName.setText(project.getName());
        txtDesc.setText(project.getDescription() != null ? project.getDescription() : "");
        txtBudget.setText(String.valueOf(project.getBudget()));
        cbPriority.setSelectedItem(project.getPriority());
        if (project.getStartDate() != null) {
            spnStartDay.setValue(project.getStartDate().getDayOfMonth());
            spnStartMonth.setValue(project.getStartDate().getMonthValue());
            spnStartYear.setValue(project.getStartDate().getYear());
        }
        if (project.getDeadline() != null) {
            spnEndDay.setValue(project.getDeadline().getDayOfMonth());
            spnEndMonth.setValue(project.getDeadline().getMonthValue());
            spnEndYear.setValue(project.getDeadline().getYear());
        }
    }

    private LocalDate getDate(JSpinner d, JSpinner m, JSpinner y) {
        return LocalDate.of((int) y.getValue(), (int) m.getValue(), (int) d.getValue());
    }

    private JPanel dateRow(JSpinner d, JSpinner m, JSpinner y) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(Color.WHITE);
        d.setPreferredSize(new Dimension(52, 26)); m.setPreferredSize(new Dimension(52, 26)); y.setPreferredSize(new Dimension(68, 26));
        p.add(d); p.add(new JLabel("/")); p.add(m); p.add(new JLabel("/")); p.add(y);
        return p;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", Font.PLAIN, 12)); l.setForeground(new Color(71, 85, 105)); return l;
    }

    private JLabel sectionTitle(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setForeground(new Color(30, 41, 59));
        l.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0)); return l;
    }

    private static JLabel buildErrorLabel() {
        JLabel l = new JLabel(""); l.setForeground(new Color(220, 38, 38)); l.setFont(new Font("SansSerif", Font.PLAIN, 11)); return l;
    }

    public boolean isSaved() { return saved; }
    public Project getProject() { return project; }

    private static class DocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;
        DocListener(Runnable r) { this.r = r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}
