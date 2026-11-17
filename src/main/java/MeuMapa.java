import java.util.ArrayList;

public class MeuMapa<K, V> {

    private static final int CAPACIDADE = 16;

    private final ArrayList<Entrada<K, V>>[] baldes;

    private static class Entrada<K, V> {
        K chave;
        V valor;

        Entrada(K chave, V valor) {
            this.chave = chave;
            this.valor = valor;
        }
    }

    @SuppressWarnings("unchecked")
    public MeuMapa() {
        baldes = (ArrayList<Entrada<K, V>>[]) new ArrayList[CAPACIDADE];
        for (int i = 0; i < CAPACIDADE; i++) {
            baldes[i] = new ArrayList<>();
        }
    }

    private int indice(K chave) {
        return Math.floorMod(chave.hashCode(), CAPACIDADE);
    }

    public void put(K chave, V valor) {
        ArrayList<Entrada<K, V>> balde = baldes[indice(chave)];
        for (Entrada<K, V> e : balde) {
            if (e.chave.equals(chave)) {
                e.valor = valor;
                return;
            }
        }
        balde.add(new Entrada<>(chave, valor));
    }

    public V get(K chave) {
        for (Entrada<K, V> e : baldes[indice(chave)]) {
            if (e.chave.equals(chave)) {
                return e.valor;
            }
        }
        return null;
    }
}
