
class Semaphore {
    private int count;
    public Semaphore(int initialCount) {
        count = initialCount; // když je 1, je to binární semafor
    }
    public synchronized void w() {
        try {
            while (count <= 0 ) wait( ); // musí být nedělitelné
            count--; // nad instancí semaforu
        } catch (InterruptedException e) { }
    }
    public synchronized void free( ) {
        count++;
        notify( );
    }
}