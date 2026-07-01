package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.service.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ProjectListPanel - Lista de Projetos (v3.0)
 *
 * Melhorias de Usabilidade:
 * - Cards visuais com status colorido (em vez de tabela crua)
 * - Botão "Ver Detalhes" leva à visão completa do projeto (com tarefas)
 * - Filtro por status integrado na barra superior
 * - Botão "Novo Projeto" sempre visível e destacado
 */
public class ProjectListPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ProjectService projectService = new ProjectService();
    private JPanel projectsGrid;
    private JComboBox<String> cbFilter;

    public ProjectListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(MainFrame.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        // ── Cabeçalho ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(28, 32, 12, 32));

        JLabel lblTitle = new JLabel("Projetos");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitle.setForeground(MainFrame.TEXT_DARK);

        // Barra de ações: filtro + botão novo
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(MainFrame.BG_MAIN);

        JLabel lblFilter = new JLabel("Filtrar por status:");
        lblFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cbFilter = new JComboBox<>(new String[]{"Todos", "PENDENTE", "EM_ANDAMENTO", "CONCLUIDO", "ATRASADO"});
        cbFilter.setPreferredSize(new Dimension(150, 32));
        cbFilter.addActionListener(e -> refresh());

        JButton btnNew = buildPrimaryButton("+ Novo Projeto");
        btnNew.addActionListener(e -> {
            ProjectDialog dialog = new ProjectDialog(mainFrame, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                projectService.addProject(dialog.getProject());
                refresh();
            }
        });

        actions.add(lblFilter);
        actions.add(cbFilter);
        actions.add(btnNew);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Grade de projetos ──────────────────────────────────────
        projectsGrid = new JPanel();
        projectsGrid.setBackground(MainFrame.BG_MAIN);
        projectsGrid.setLayout(new BoxLayout(projectsGrid, BoxLayout.Y_AXIS));
        projectsGrid.setBorder(BorderFactory.createEmptyBorder(0, 32, 32, 32));

        JScrollPane scroll = new JScrollPane(projectsGrid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(MainFrame.BG_MAIN);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        projectsGrid.removeAll();
        List<Project> projects = Database.getInstance().getProjects();
        String filter = (String) cbFilter.getSelectedItem();

        boolean hasAny = false;
        for (Project p : projects) {
            if ("Todos".equals(filter) || p.getStatus().name().equals(filter)) {
                projectsGrid.add(buildProjectCard(p));
                projectsGrid.add(Box.createVerticalStrut(12));
                hasAny = true;
            }
        }

        if (!hasAny) {
            JLabel lblEmpty = new JLabel("Nenhum projeto encontrado. Clique em '+ Novo Projeto' para comecar.");
            lblEmpty.setFont(new Font("SansSerif", Font.PLAIN, 14));
            lblEmpty.setForeground(new Color(148, 163, 184));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            projectsGrid.add(Box.createVerticalStrut(60));
            projectsGrid.add(lblEmpty);
        }

        projectsGrid.revalidate();
        projectsGrid.repaint();
    }

    // ── Card de Projeto ────────────────────────────────────────────
    private JPanel buildProjectCard(Project p) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Barra lateral colorida por prioridade
        Color priorityColor = getPriorityColor(p.getPriority());
        JPanel bar = new JPanel();
        bar.setBackground(priorityColor);
        bar.setPreferredSize(new Dimension(5, 0));

        // Informações principais
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setBackground(Color.WHITE);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setBackground(Color.WHITE);

        JLabel lblName = new JLabel(p.getName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblName.setForeground(MainFrame.TEXT_DARK);

        JLabel lblStatus = buildStatusBadge(p.getStatus());
        JLabel lblPriority = buildPriorityBadge(p.getPriority());

        topRow.add(lblName);
        topRow.add(lblStatus);
        topRow.add(lblPriority);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        bottomRow.setBackground(Color.WHITE);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String deadlineStr = p.getDeadline() != null ? p.getDeadline().format(dtf) : "—";
        String startStr    = p.getStartDate() != null ? p.getStartDate().format(dtf) : "—";
        String budget      = String.format("R$ %.2f", p.getBudget());

        bottomRow.add(buildInfoChip("Inicio: " + startStr));
        bottomRow.add(buildInfoChip("Prazo: " + deadlineStr));
        bottomRow.add(buildInfoChip("Orcamento: " + budget));

        // Contar tarefas do projeto
        long taskCount = Database.getInstance().getTasks().stream()
                .filter(t -> t.getProjectId().equals(p.getId())).count();
        bottomRow.add(buildInfoChip("Tarefas: " + taskCount));

        info.add(topRow);
        info.add(bottomRow);

        // Botões de ação
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);

        JButton btnDetail = buildPrimaryButton("Ver Detalhes");
        btnDetail.addActionListener(e -> mainFrame.openProjectDetail(p));

        JButton btnEdit = buildSecondaryButton("Editar");
        btnEdit.addActionListener(e -> {
            ProjectDialog dialog = new ProjectDialog(mainFrame, p);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                Database.getInstance().saveProjects();
                refresh();
            }
        });

        JButton btnDelete = buildDangerButton("Excluir");
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Deseja excluir o projeto '" + p.getName() + "'?\nEsta acao nao pode ser desfeita.",
                    "Confirmar Exclusao", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                Database.getInstance().getProjects().removeIf(proj -> proj.getId().equals(p.getId()));
                // Remover tarefas vinculadas
                Database.getInstance().getTasks().removeIf(t -> t.getProjectId().equals(p.getId()));
                Database.getInstance().saveProjects();
                Database.getInstance().saveTasks();
                refresh();
            }
        });

        actions.add(btnDetail);
        actions.add(btnEdit);
        actions.add(btnDelete);

        card.add(bar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    // ── Badges e chips ─────────────────────────────────────────────
    private JLabel buildStatusBadge(Enums.Status status) {
        JLabel lbl = new JLabel(status.name().replace("_", " "));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        switch (status) {
            case CONCLUIDO:    lbl.setBackground(new Color(16, 185, 129)); break;
            case EM_ANDAMENTO: lbl.setBackground(new Color(59, 130, 246)); break;
            case ATRASADO:     lbl.setBackground(new Color(239, 68, 68));  break;
            default:           lbl.setBackground(new Color(148, 163, 184)); break;
        }
        return lbl;
    }

    private JLabel buildPriorityBadge(Enums.Priority priority) {
        JLabel lbl = new JLabel(priority.name());
        lbl.setOpaque(true);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        switch (priority) {
            case ALTA:  lbl.setBackground(new Color(254, 226, 226)); lbl.setForeground(new Color(185, 28, 28)); break;
            case MEDIA: lbl.setBackground(new Color(254, 243, 199)); lbl.setForeground(new Color(146, 64, 14)); break;
            default:    lbl.setBackground(new Color(220, 252, 231)); lbl.setForeground(new Color(21, 128, 61));  break;
        }
        return lbl;
    }

    private JLabel buildInfoChip(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(100, 116, 139));
        return lbl;
    }

    private Color getPriorityColor(Enums.Priority p) {
        switch (p) {
            case ALTA:  return new Color(239, 68, 68);
            case MEDIA: return new Color(245, 158, 11);
            default:    return new Color(16, 185, 129);
        }
    }

    // ── Botões ─────────────────────────────────────────────────────
    private JButton buildPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(MainFrame.ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private JButton buildSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(MainFrame.TEXT_DARK);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private JButton buildDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(254, 242, 242));
        btn.setForeground(new Color(185, 28, 28));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }
}
