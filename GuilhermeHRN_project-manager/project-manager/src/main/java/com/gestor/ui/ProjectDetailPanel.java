package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.service.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ProjectDetailPanel - Visão Detalhada de um Projeto
 *
 * Melhorias de Usabilidade:
 * - Unifica dados do projeto, progresso de fases e tarefas em uma única tela
 * - Linha do tempo visual das fases (Requisitos / Dev / Deploy)
 * - Progresso calculado automaticamente pelas tarefas concluídas
 * - Botão "Voltar" claro para retornar à lista de projetos
 * - Adição de tarefas diretamente nesta tela (sem trocar de aba)
 */
public class ProjectDetailPanel extends JPanel {

    private final MainFrame mainFrame;
    private Project project;
    private final ProjectService projectService = new ProjectService();

    private JPanel tasksContainer;
    private JLabel lblProgress;
    private JProgressBar progressBar;

    public ProjectDetailPanel(MainFrame mainFrame, Project project) {
        this.mainFrame = mainFrame;
        this.project   = project;
        setLayout(new BorderLayout());
        setBackground(MainFrame.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        // ── Cabeçalho com botão Voltar ─────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(20, 32, 10, 32));

        JButton btnBack = new JButton("< Voltar para Projetos");
        btnBack.setBackground(MainFrame.BG_MAIN);
        btnBack.setForeground(MainFrame.ACCENT);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> mainFrame.navigateTo("projects", mainFrame.btnProjects));

        header.add(btnBack, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Corpo rolável ──────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(MainFrame.BG_MAIN);
        body.setBorder(BorderFactory.createEmptyBorder(0, 32, 32, 32));

        body.add(buildProjectInfoCard());
        body.add(Box.createVerticalStrut(20));
        body.add(buildPhasesCard());
        body.add(Box.createVerticalStrut(20));
        body.add(buildTasksCard());

        JScrollPane scroll = new JScrollPane(body,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(MainFrame.BG_MAIN);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Card de Informações do Projeto ─────────────────────────────
    private JPanel buildProjectInfoCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout(0, 12));

        // Título + status
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setBackground(Color.WHITE);

        JLabel lblName = new JLabel(project.getName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblName.setForeground(MainFrame.TEXT_DARK);

        JLabel lblStatus = buildBadge(project.getStatus().name().replace("_", " "),
                getStatusColor(project.getStatus()));

        JButton btnEdit = buildSmallButton("Editar Projeto", MainFrame.ACCENT);
        btnEdit.addActionListener(e -> {
            ProjectDialog dialog = new ProjectDialog(mainFrame, project);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                Database.getInstance().saveProjects();
                refreshAll();
            }
        });

        titleRow.add(lblName);
        titleRow.add(lblStatus);
        titleRow.add(Box.createHorizontalStrut(20));
        titleRow.add(btnEdit);

        // Grade de detalhes
        JPanel details = new JPanel(new GridLayout(2, 4, 16, 8));
        details.setBackground(Color.WHITE);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        details.add(buildDetailItem("Descricao", project.getDescription() != null ? project.getDescription() : "—"));
        details.add(buildDetailItem("Data de Inicio", project.getStartDate() != null ? project.getStartDate().format(dtf) : "—"));
        details.add(buildDetailItem("Prazo Final", project.getDeadline() != null ? project.getDeadline().format(dtf) : "—"));
        details.add(buildDetailItem("Orcamento", String.format("R$ %.2f", project.getBudget())));
        details.add(buildDetailItem("Prioridade", project.getPriority().name()));
        details.add(buildDetailItem("Responsavel", project.getOwnerId() != null ? project.getOwnerId() : "—"));

        // Dias restantes
        String diasRestantes = "—";
        if (project.getDeadline() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), project.getDeadline());
            diasRestantes = dias >= 0 ? dias + " dias restantes" : Math.abs(dias) + " dias de atraso";
        }
        details.add(buildDetailItem("Situacao", diasRestantes));

        // Progresso geral
        int prog = calcProgress();
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(prog);
        progressBar.setStringPainted(true);
        progressBar.setString(prog + "% concluido");
        progressBar.setForeground(new Color(16, 185, 129));
        progressBar.setBackground(new Color(226, 232, 240));
        progressBar.setPreferredSize(new Dimension(0, 22));

        lblProgress = new JLabel("Progresso Geral: " + prog + "%");
        lblProgress.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblProgress.setForeground(MainFrame.TEXT_DARK);

        JPanel progressSection = new JPanel(new BorderLayout(0, 4));
        progressSection.setBackground(Color.WHITE);
        progressSection.add(lblProgress, BorderLayout.NORTH);
        progressSection.add(progressBar, BorderLayout.CENTER);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(details, BorderLayout.CENTER);
        card.add(progressSection, BorderLayout.SOUTH);
        return card;
    }

    // ── Card de Fases do Projeto ───────────────────────────────────
    private JPanel buildPhasesCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout(0, 14));

        JLabel lblTitle = new JLabel("Cronograma de Fases (distribuicao 20% / 60% / 20%)");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitle.setForeground(MainFrame.TEXT_DARK);

        JPanel phasesRow = new JPanel(new GridLayout(1, 3, 12, 0));
        phasesRow.setBackground(Color.WHITE);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        phasesRow.add(buildPhaseBlock("1. Requisitos (20%)",
                project.getStartDate() != null ? project.getStartDate().format(dtf) : "—",
                project.getRequirementsDeadline() != null ? project.getRequirementsDeadline().format(dtf) : "—",
                project.isRequirementsDone(),
                new Color(139, 92, 246)));

        phasesRow.add(buildPhaseBlock("2. Desenvolvimento (60%)",
                project.getRequirementsDeadline() != null ? project.getRequirementsDeadline().format(dtf) : "—",
                project.getDevelopmentDeadline() != null ? project.getDevelopmentDeadline().format(dtf) : "—",
                project.isDevelopmentDone(),
                new Color(59, 130, 246)));

        phasesRow.add(buildPhaseBlock("3. Implantacao (20%)",
                project.getDevelopmentDeadline() != null ? project.getDevelopmentDeadline().format(dtf) : "—",
                project.getDeadline() != null ? project.getDeadline().format(dtf) : "—",
                project.isDeploymentDone(),
                new Color(16, 185, 129)));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(phasesRow, BorderLayout.CENTER);

        // Nota explicativa
        JLabel lblNote = new JLabel("As datas das fases sao calculadas automaticamente ao criar/editar o projeto.");
        lblNote.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblNote.setForeground(new Color(148, 163, 184));
        card.add(lblNote, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildPhaseBlock(String title, String start, String end, boolean done, Color color) {
        JPanel block = new JPanel(new BorderLayout(0, 6));
        block.setBackground(done ? new Color(240, 253, 244) : new Color(248, 250, 252));
        block.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(done ? new Color(134, 239, 172) : new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel topBar = new JPanel();
        topBar.setBackground(color);
        topBar.setPreferredSize(new Dimension(0, 3));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitle.setForeground(MainFrame.TEXT_DARK);

        JLabel lblDates = new JLabel("<html><span style='color:#64748b'>De: " + start
                + "</span><br><span style='color:#64748b'>Ate: " + end + "</span></html>");
        lblDates.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JCheckBox chkDone = new JCheckBox(done ? "Concluida" : "Em andamento");
        chkDone.setSelected(done);
        chkDone.setBackground(done ? new Color(240, 253, 244) : new Color(248, 250, 252));
        chkDone.setFont(new Font("SansSerif", Font.BOLD, 12));
        chkDone.setForeground(done ? new Color(21, 128, 61) : new Color(100, 116, 139));
        chkDone.addActionListener(e -> {
            if (title.contains("Requisitos"))    project.setRequirementsDone(chkDone.isSelected());
            else if (title.contains("Desenvolv")) project.setDevelopmentDone(chkDone.isSelected());
            else                                  project.setDeploymentDone(chkDone.isSelected());
            Database.getInstance().saveProjects();
            refreshAll();
        });

        block.add(topBar, BorderLayout.NORTH);
        block.add(lblTitle, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(block.getBackground());
        bottom.add(lblDates, BorderLayout.NORTH);
        bottom.add(chkDone, BorderLayout.SOUTH);
        block.add(bottom, BorderLayout.SOUTH);
        return block;
    }

    // ── Card de Tarefas ────────────────────────────────────────────
    private JPanel buildTasksCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Tarefas do Projeto");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitle.setForeground(MainFrame.TEXT_DARK);

        JButton btnAddTask = buildSmallButton("+ Nova Tarefa", MainFrame.ACCENT);
        btnAddTask.addActionListener(e -> addTask());

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(btnAddTask, BorderLayout.EAST);

        tasksContainer = new JPanel();
        tasksContainer.setLayout(new BoxLayout(tasksContainer, BoxLayout.Y_AXIS));
        tasksContainer.setBackground(Color.WHITE);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(tasksContainer, BorderLayout.CENTER);

        refreshTasks();
        return card;
    }

    private void refreshTasks() {
        tasksContainer.removeAll();
        List<Task> tasks = Database.getInstance().getTasks();
        boolean hasTask = false;

        for (Task t : tasks) {
            if (t.getProjectId().equals(project.getId())) {
                tasksContainer.add(buildTaskRow(t));
                tasksContainer.add(Box.createVerticalStrut(6));
                hasTask = true;
            }
        }

        if (!hasTask) {
            JLabel lblEmpty = new JLabel("Nenhuma tarefa cadastrada. Clique em '+ Nova Tarefa' para adicionar.");
            lblEmpty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblEmpty.setForeground(new Color(148, 163, 184));
            tasksContainer.add(lblEmpty);
        }

        tasksContainer.revalidate();
        tasksContainer.repaint();
    }

    private JPanel buildTaskRow(Task t) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // Checkbox de conclusão
        JCheckBox chkDone = new JCheckBox();
        chkDone.setSelected(t.getStatus() == Enums.Status.CONCLUIDO);
        chkDone.setBackground(new Color(248, 250, 252));
        chkDone.addActionListener(e -> {
            t.setStatus(chkDone.isSelected() ? Enums.Status.CONCLUIDO : Enums.Status.PENDENTE);
            Database.getInstance().saveTasks();
            refreshAll();
        });

        JLabel lblTitle = new JLabel(t.getTitle());
        lblTitle.setFont(new Font("SansSerif", t.getStatus() == Enums.Status.CONCLUIDO ? Font.ITALIC : Font.PLAIN, 13));
        lblTitle.setForeground(t.getStatus() == Enums.Status.CONCLUIDO
                ? new Color(148, 163, 184) : MainFrame.TEXT_DARK);

        JLabel lblPriority = buildBadge(t.getPriority().name(), getPriorityColor(t.getPriority()));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(new Color(248, 250, 252));
        left.add(chkDone);
        left.add(lblTitle);
        left.add(lblPriority);

        JButton btnDel = new JButton("Remover");
        btnDel.setBackground(new Color(254, 242, 242));
        btnDel.setForeground(new Color(185, 28, 28));
        btnDel.setBorderPainted(false);
        btnDel.setFocusPainted(false);
        btnDel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDel.addActionListener(e -> {
            Database.getInstance().getTasks().removeIf(task -> task.getId().equals(t.getId()));
            Database.getInstance().saveTasks();
            refreshAll();
        });

        row.add(left, BorderLayout.CENTER);
        row.add(btnDel, BorderLayout.EAST);
        return row;
    }

    private void addTask() {
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        JTextField txtTitle = new JTextField();
        JTextField txtDesc  = new JTextField();
        JComboBox<Enums.Priority> cbPriority = new JComboBox<>(Enums.Priority.values());

        form.add(new JLabel("Titulo da Tarefa:"));
        form.add(txtTitle);
        form.add(new JLabel("Descricao:"));
        form.add(txtDesc);
        form.add(new JLabel("Prioridade:"));
        form.add(cbPriority);

        int result = JOptionPane.showConfirmDialog(mainFrame, form,
                "Nova Tarefa — " + project.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION && !txtTitle.getText().trim().isEmpty()) {
            Task t = new Task(txtTitle.getText().trim(), txtDesc.getText().trim(),
                    (Enums.Priority) cbPriority.getSelectedItem(), project.getId());
            projectService.addTask(t);
            refreshAll();
        }
    }

    // ── Utilitários ────────────────────────────────────────────────
    private void refreshAll() {
        // Recarregar o projeto da base para ter dados atualizados
        project = Database.getInstance().getProjects().stream()
                .filter(p -> p.getId().equals(project.getId()))
                .findFirst().orElse(project);

        // Reconstruir toda a UI do painel
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private int calcProgress() {
        List<Task> tasks = Database.getInstance().getTasks().stream()
                .filter(t -> t.getProjectId().equals(project.getId()))
                .collect(java.util.stream.Collectors.toList());
        if (tasks.isEmpty()) return 0;
        long done = tasks.stream().filter(t -> t.getStatus() == Enums.Status.CONCLUIDO).count();
        return (int) ((done * 100) / tasks.size());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JLabel buildBadge(String text, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return lbl;
    }

    private JPanel buildDetailItem(String label, String value) {
        JPanel item = new JPanel(new BorderLayout(0, 2));
        item.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(new Color(100, 116, 139));
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 13));
        val.setForeground(MainFrame.TEXT_DARK);
        item.add(lbl, BorderLayout.NORTH);
        item.add(val, BorderLayout.CENTER);
        return item;
    }

    private JButton buildSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private Color getStatusColor(Enums.Status s) {
        switch (s) {
            case CONCLUIDO:    return new Color(16, 185, 129);
            case EM_ANDAMENTO: return new Color(59, 130, 246);
            case ATRASADO:     return new Color(239, 68, 68);
            default:           return new Color(148, 163, 184);
        }
    }

    private Color getPriorityColor(Enums.Priority p) {
        switch (p) {
            case ALTA:  return new Color(239, 68, 68);
            case MEDIA: return new Color(245, 158, 11);
            default:    return new Color(16, 185, 129);
        }
    }
}
