package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.service.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardPanel - Tela inicial do sistema (v3.0)
 *
 * Melhorias de Usabilidade:
 * - Tela inicial ao logar (antes era a 3ª aba)
 * - Cards de métricas com cores e contexto visual
 * - Lista de projetos com prazo próximo / atrasados
 * - Sugestão de prazo integrada na tela, sem popup solto
 */
public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ProjectService projectService = new ProjectService();
    private JPanel cardsRow;
    private JPanel alertsPanel;
    private JLabel lblSuggestion;

    public DashboardPanel(MainFrame mainFrame) {
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

        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitle.setForeground(MainFrame.TEXT_DARK);

        JLabel lblDate = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",
                new java.util.Locale("pt", "BR"))));
        lblDate.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblDate.setForeground(new Color(100, 116, 139));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblDate, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Corpo rolável ──────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(MainFrame.BG_MAIN);
        body.setBorder(BorderFactory.createEmptyBorder(0, 32, 32, 32));

        // Linha de cards de métricas
        cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setBackground(MainFrame.BG_MAIN);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        body.add(cardsRow);
        body.add(Box.createVerticalStrut(24));

        // Painel de sugestão de prazo (integrado, sem popup)
        JPanel suggestionPanel = buildSuggestionPanel();
        suggestionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        body.add(suggestionPanel);
        body.add(Box.createVerticalStrut(24));

        // Alertas: projetos atrasados ou com prazo próximo
        JLabel lblAlertsTitle = new JLabel("Projetos que precisam de atenção");
        lblAlertsTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAlertsTitle.setForeground(MainFrame.TEXT_DARK);
        lblAlertsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lblAlertsTitle);
        body.add(Box.createVerticalStrut(10));

        alertsPanel = new JPanel();
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        alertsPanel.setBackground(MainFrame.BG_MAIN);
        body.add(alertsPanel);

        add(new JScrollPane(body,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }

    private JPanel buildSuggestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(new Color(239, 246, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel lblInfo = new JLabel("<html><b>Sugestão Inteligente de Prazo</b><br>"
                + "<span style='color:#64748b'>Baseada no histórico de projetos concluídos</span></html>");
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        lblSuggestion = new JLabel("Clique em 'Calcular' para ver a sugestão");
        lblSuggestion.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSuggestion.setForeground(MainFrame.ACCENT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(new Color(239, 246, 255));

        JSpinner spinnerDays = new JSpinner(new SpinnerNumberModel(10, 1, 365, 1));
        spinnerDays.setPreferredSize(new Dimension(70, 28));
        JLabel lblDaysLabel = new JLabel("dias:");
        JButton btnCalc = buildSmallButton("Calcular", MainFrame.ACCENT);
        btnCalc.addActionListener(e -> {
            int days = (int) spinnerDays.getValue();
            LocalDate suggestion = projectService.suggestDeadline(days);
            lblSuggestion.setText("Para " + days + " dias: prazo sugerido = "
                    + suggestion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });

        right.add(lblDaysLabel);
        right.add(spinnerDays);
        right.add(btnCalc);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setBackground(new Color(239, 246, 255));
        left.add(lblInfo, BorderLayout.NORTH);
        left.add(lblSuggestion, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ── Atualização de dados ───────────────────────────────────────
    public void refresh() {
        List<Project> projects = Database.getInstance().getProjects();
        List<Task>    tasks    = Database.getInstance().getTasks();

        long atrasados = projects.stream()
                .filter(p -> p.getStatus() == Enums.Status.ATRASADO).count();
        long emAndamento = projects.stream()
                .filter(p -> p.getStatus() == Enums.Status.EM_ANDAMENTO).count();
        long concluidos = projects.stream()
                .filter(p -> p.getStatus() == Enums.Status.CONCLUIDO).count();
        long pendentes = tasks.stream()
                .filter(t -> t.getStatus() == Enums.Status.PENDENTE).count();

        // Atualizar cards
        cardsRow.removeAll();
        cardsRow.add(buildMetricCard("Total de Projetos",
                String.valueOf(projects.size()), new Color(59, 130, 246), "projetos cadastrados"));
        cardsRow.add(buildMetricCard("Em Andamento",
                String.valueOf(emAndamento), new Color(16, 185, 129), "projetos ativos"));
        cardsRow.add(buildMetricCard("Atrasados",
                String.valueOf(atrasados), new Color(239, 68, 68), "precisam de atencao"));
        cardsRow.add(buildMetricCard("Tarefas Pendentes",
                String.valueOf(pendentes), new Color(245, 158, 11), "aguardando execucao"));
        cardsRow.revalidate();
        cardsRow.repaint();

        // Atualizar alertas
        alertsPanel.removeAll();
        LocalDate hoje = LocalDate.now();
        List<Project> alertProjects = projects.stream()
                .filter(p -> p.getStatus() != Enums.Status.CONCLUIDO
                        && p.getDeadline() != null
                        && (p.getDeadline().isBefore(hoje) || p.getDeadline().isBefore(hoje.plusDays(7))))
                .collect(Collectors.toList());

        if (alertProjects.isEmpty()) {
            JLabel lblOk = new JLabel("  Nenhum projeto atrasado ou com prazo critico. Tudo em dia!");
            lblOk.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblOk.setForeground(new Color(16, 185, 129));
            alertsPanel.add(lblOk);
        } else {
            for (Project p : alertProjects) {
                alertsPanel.add(buildAlertRow(p, hoje));
                alertsPanel.add(Box.createVerticalStrut(8));
            }
        }
        alertsPanel.revalidate();
        alertsPanel.repaint();
    }

    // ── Componentes visuais ────────────────────────────────────────
    private JPanel buildMetricCard(String title, String value, Color accentColor, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        // Barra colorida no topo
        JPanel topBar = new JPanel();
        topBar.setBackground(accentColor);
        topBar.setPreferredSize(new Dimension(0, 4));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblValue.setForeground(accentColor);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitle.setForeground(MainFrame.TEXT_DARK);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(new Color(148, 163, 184));

        JPanel textPanel = new JPanel(new BorderLayout(0, 2));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(lblTitle, BorderLayout.NORTH);
        textPanel.add(lblValue, BorderLayout.CENTER);
        textPanel.add(lblSub, BorderLayout.SOUTH);

        card.add(topBar, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAlertRow(Project p, LocalDate hoje) {
        boolean atrasado = p.getDeadline().isBefore(hoje);
        Color bgColor = atrasado ? new Color(254, 242, 242) : new Color(255, 251, 235);
        Color borderColor = atrasado ? new Color(252, 165, 165) : new Color(253, 230, 138);
        Color tagColor = atrasado ? new Color(239, 68, 68) : new Color(245, 158, 11);
        String tagText = atrasado ? "ATRASADO" : "PRAZO PROXIMO";

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(bgColor);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel lblTag = new JLabel(tagText);
        lblTag.setForeground(Color.WHITE);
        lblTag.setBackground(tagColor);
        lblTag.setOpaque(true);
        lblTag.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblTag.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel lblName = new JLabel(p.getName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblName.setForeground(MainFrame.TEXT_DARK);

        String deadlineStr = p.getDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel lblDeadline = new JLabel("Prazo: " + deadlineStr);
        lblDeadline.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDeadline.setForeground(new Color(100, 116, 139));

        JButton btnVer = buildSmallButton("Ver Projeto", MainFrame.ACCENT);
        btnVer.addActionListener(e -> mainFrame.openProjectDetail(p));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(bgColor);
        left.add(lblTag);
        left.add(lblName);
        left.add(lblDeadline);

        row.add(left, BorderLayout.CENTER);
        row.add(btnVer, BorderLayout.EAST);
        return row;
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
}
