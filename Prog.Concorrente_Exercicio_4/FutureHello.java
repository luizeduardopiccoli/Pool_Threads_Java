/* Disciplina: Programacao Concorrente */
/* Prof.: Silvana Rossetto */
/* Aluno: Luiz Eduardo Lahm Piccoli */
/* Laboratório: 11 */
/* Codigo: Exemplo de uso de futures (modificado para contar primos e medir tempo) */
/* -------------------------------------------------------------------*/

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;

// Atividade 3, Item 2: Tarefa Callable para verificar se um número é primo
class PrimoCallable implements Callable<Integer> {
  private final int n;

  // Construtor
  public PrimoCallable(int n) { this.n = n; }

  // Função para determinar se um número é primo
  private static int ehPrimo(int n) {
      if (n <= 1) return 0;
      if (n == 2) return 1;
      if (n % 2 == 0) return 0;
      for (int i = 3; i * i <= n; i += 2) {
          if (n % i == 0) return 0;
      }
      return 1;
  }
 
  /**
   * Método de execução da tarefa.
   * @return 1 se o número for primo, 0 caso contrário.
   */
  public Integer call() throws Exception {
    return ehPrimo(this.n);
  }
}


//classe do método main
public class FutureHello  {
  // N_LIMITE é o N mencionado na Atividade 3, Item 3
  private static final int N_LIMITE = 1000000; // Limite padrão para testes de concorrência
  private static final int NTHREADS = 4;

  public static void main(String[] args) {
    
    // --- INÍCIO DA MEDIÇÃO DE TEMPO ---
    long startTime = System.nanoTime(); // Captura o tempo inicial
    
    //cria um pool de threads (NTHREADS)
    ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
    //cria uma lista para armazenar referencias de chamadas assincronas (Futures)
    List<Future<Integer>> list = new ArrayList<Future<Integer>>();

    // Atividade 3, Item 3: Dispara tarefas de checagem de primalidade para o intervalo
    for (int i = 1; i <= N_LIMITE; i++) {
      Callable<Integer> worker = new PrimoCallable(i);
      Future<Integer> submit = executor.submit(worker);
      list.add(submit);
    }

    // Recupera os resultados e faz o somatório final (contagem de primos)
    int countPrimes = 0;
    for (Future<Integer> future : list) {
      try {
        // future.get() retorna 1 (primo) ou 0 (não primo). A soma é a contagem total.
        countPrimes += future.get(); // Bloqueia se a computação não tiver terminado
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
    
    // Encerra o pool de threads
    executor.shutdown();
    
    // --- FIM DA MEDIÇÃO DE TEMPO ---
    long endTime = System.nanoTime(); // Captura o tempo final
    // Converte para milissegundos
    long duration = (endTime - startTime) / 1_000_000; 
    
    System.out.println("Total de números verificados: " + N_LIMITE);
    System.out.println("Quantidade de números primos no intervalo de 1 a " + N_LIMITE + ": " + countPrimes);
    System.out.println("Tempo de execução total (milisegundos): " + duration + "ms");
  }
}