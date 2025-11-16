/* Disciplina: Programacao Concorrente */
/* Prof.: Silvana Rossetto */
/* Aluno: Luiz Eduardo Lahm Piccoli */
/* Laboratório: 11 */
/* Codigo: Criando um pool de threads em Java */

import java.util.LinkedList;

//-------------------------------------------------------------------------------
/**
 * Classe que implementa um pool de threads simples e customizado.
 * Ela mantém um conjunto fixo de threads de trabalho (MyPoolThreads) 
 * que consomem tarefas (objetos Runnable) de uma fila interna (LinkedList).
 */
class FilaTarefas {
    private final int nThreads; // Número de threads criadas no pool.
    private final MyPoolThreads[] threads; // Array contendo as threads de trabalho.
    private final LinkedList<Runnable> queue; // Fila onde as tarefas são depositadas.
    private boolean shutdown; // Flag que sinaliza que o pool deve encerrar suas atividades.

    /**
     * Construtor da FilaTarefas.
     * Inicializa a fila, cria o número especificado de threads e as inicia.
     * @param nThreads O número de threads de trabalho a serem criadas no pool.
     */
    public FilaTarefas(int nThreads) {
        this.shutdown = false;
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new MyPoolThreads[nThreads];
        for (int i=0; i<nThreads; i++) {
            threads[i] = new MyPoolThreads();
            threads[i].start();
        } 
    }

    /**
     * Adiciona uma tarefa para ser executada pelo pool. 
     * A tarefa é inserida na fila e notifica-se uma thread para processá-la.
     * @param r A tarefa Runnable a ser executada.
     */
    public void execute(Runnable r) {
        synchronized(queue) {
            if (this.shutdown) return;
            queue.addLast(r);
            queue.notify();
        }
    }
    
    /**
     * Inicia o processo de encerramento do pool. 
     * Marca o pool para shutdown, notifica todas as threads e espera (join) 
     * pelo término seguro de cada uma delas.
     */
    public void shutdown() {
        synchronized(queue) {
            this.shutdown=true;
            queue.notifyAll();
        }
        for (int i=0; i<nThreads; i++) {
          try { threads[i].join(); } catch (InterruptedException e) { return; }
        }
    }

    /**
     * Classe interna que representa uma thread de trabalho do pool.
     * Ela entra em um loop infinito, esperando por tarefas na fila e as executando.
     */
    private class MyPoolThreads extends Thread {
       public void run() {
         Runnable r;
         while (true) {
           synchronized(queue) {
             // Espera se a fila estiver vazia e o pool não estiver em shutdown
             while (queue.isEmpty() && (!shutdown)) {
               try { queue.wait(); }
               catch (InterruptedException ignored){}
             }
             // Condição de saída: fila vazia e pool em shutdown
             if (queue.isEmpty()) return;   
             r = (Runnable) queue.removeFirst();
           }
           // Executa a tarefa fora da seção crítica
           try { r.run(); }
           catch (RuntimeException e) {}
         } 
       } 
    } 
}
//-------------------------------------------------------------------------------

//--PASSO 1: cria uma classe que implementa a interface Runnable 
class Hello implements Runnable {
   String msg;
   public Hello(String m) { msg = m; }

   //--metodo executado pela thread
   public void run() {
      System.out.println(msg);
   }
}

class Primo implements Runnable {
   private int n;

   // Construtor: recebe o número a ser testado
   public Primo(int n) { this.n = n; }

   /**
    * Verifica se um número inteiro positivo é primo.
    * Baseado na lógica fornecida no enunciado.
    * @param n O número a ser verificado.
    * @return 1 se o número for primo, 0 caso contrário.
    */
   private static int ehPrimo(int n) {
       if (n <= 1) return 0;
       if (n == 2) return 1;
       if (n % 2 == 0) return 0;
       // Verifica divisores ímpares até a raiz quadrada de n
       for (int i = 3; i * i <= n; i += 2) {
           if (n % i == 0) return 0;
       }
       return 1;
   }

   // método executado pela thread: realiza a checagem e imprime
   public void run() {
      int resultado = ehPrimo(this.n);
      String status = (resultado == 1) ? "é primo" : "não é primo";
      System.out.println("O número " + this.n + " " + status);
   }
}

//Classe da aplicação (método main)
class MyPool {
    private static final int NTHREADS = 10;

    public static void main (String[] args) {
      //--PASSO 2: cria o pool de threads
      FilaTarefas pool = new FilaTarefas(NTHREADS); 
      
      //--PASSO 3: dispara a execução dos objetos runnable usando o pool de threads
      for (int i = 0; i < 25; i++) {
        // As tarefas Hello estão comentadas, conforme solicitado em 1.4:
        // final String m = "Hello da tarefa " + i;
        // Runnable hello = new Hello(m);
        // pool.execute(hello);
        
        // Dispara a nova tarefa Primo:
        Runnable primo = new Primo(i);
        pool.execute(primo);
      }

      //--PASSO 4: esperar pelo termino das threads
      pool.shutdown();
      System.out.println("Terminou");
   }
}