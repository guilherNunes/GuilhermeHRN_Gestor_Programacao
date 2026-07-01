package com.gestor.model;

/**
 * Esta classe funciona como um "Dicionário de Opções" do sistema.
 * Em vez de deixar o usuário escrever qualquer coisa, nós damos opções fixas
 * para evitar erros de digitação e manter a organização.
 */
public class Enums {
    
    // Define o que cada pessoa pode fazer no sistema (O "Cargo" dela)
    public enum UserRole { 
        ADMIN,    // Tem poder total sobre tudo
        GERENTE,  // Pode cuidar dos projetos
        USUARIO   // Só pode ver e fazer o básico
    }
    
    // Define o quão urgente é uma tarefa (A "Importância")
    public enum Priority { 
        BAIXA, 
        MEDIA, 
        ALTA 
    }
    
    // Define em que pé está o projeto ou tarefa (O "Estado" atual)
    public enum Status { 
        PENDENTE,      // Ainda não começou
        EM_ANDAMENTO,  // Está sendo feito agora
        CONCLUIDO,     // Já terminou
        ATRASADO       // Passou do prazo
    }
    
    // Define que tipo de ação foi feita para ficar guardado no histórico
    public enum LogAction { 
        CRIACAO, 
        ALTERACAO, 
        EXCLUSAO, 
        LOGIN 
    }
    
    // Define com o que o tempo foi gasto
    public enum TimeType { 
        TRABALHO, 
        REUNIAO, 
        ESTUDO 
    }
}
