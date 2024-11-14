import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Connection conexao = ConexaoMySql.conectar();

        if (conexao != null) {
            System.out.println("Conexão estabelecida com sucesso!");
        } else {
            System.out.println("Falha ao estabelecer conexão.");
        }

        ConexaoMySql.fecharConexao(conexao);

        // Aqui

        System.out.println("Olá meu jovem, você está na sessão de alterar o limite de crédito");
        System.out.println("");
        System.out.println("Qual o id do cliente que deseja alterar?");
        int clienteId  = scanner.nextInt();

        System.out.print("Digite o novo limite de crédito: ");
        double novoLimiteCredito = scanner.nextDouble();

        try (Connection conn = DriverManager.getConnection(ConexaoMySql.URL, ConexaoMySql.USUARIO, ConexaoMySql.SENHA)) {
            conn.setAutoCommit(false);
            PreparedStatement stmtTimeout = conn.prepareStatement("SET SESSION innodb_lock_wait_timeout = 3");
            stmtTimeout.execute();
            String sql = "UPDATE cliente SET limite_credito = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, novoLimiteCredito);
                stmt.setInt(2, clienteId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Limite de crédito atualizado com sucesso!");

                    System.out.println("Deseja confirmar a atualização? (S/N)");
                    String confirmacao = scanner.next();

                    if (confirmacao.equalsIgnoreCase("S")) {
                        conn.commit();
                        System.out.println("Transação confirmada!");
                    } else {
                        conn.rollback();
                        System.out.println("Transação cancelada, alterações não foram feitas.");
                    }
                } else {
                    System.out.println("Cliente com ID " + clienteId + " não encontrado.");
                    conn.rollback();
                }
            } catch (SQLException e) {
                System.out.println("Erro ao atualizar o limite de crédito: " + e.getMessage());
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Erro ao realizar rollback: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao estabelecer a conexão: " + e.getMessage());
        }

        scanner.close();
    }
}
