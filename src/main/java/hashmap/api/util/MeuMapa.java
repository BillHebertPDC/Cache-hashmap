package hashmap.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeuMapa<K, V> {

    private static final int CAPACIDADE_INICIAL = 16;
    private static final double FATOR_CARGA_MAXIMO = 0.75;

    private ArrayList<No<K, V>>[] baldes;
    private int tamanho;

    public record Entrada<K, V>(K chave, V valor) {}

    private static class No<K, V> {
        final K chave;
        V valor;

        No(K chave, V valor) {
            this.chave = chave;
            this.valor = valor;
        }
    }

    @SuppressWarnings("unchecked")
    public MeuMapa() {
        baldes = (ArrayList<No<K, V>>[]) new ArrayList[CAPACIDADE_INICIAL];
        for (int i = 0; i < CAPACIDADE_INICIAL; i++) {
            baldes[i] = new ArrayList<>();
        }
    }

    private int indice(K chave) {
        return Math.floorMod(Objects.hashCode(chave), baldes.length);
    }

    public V put(K chave, V valor) {
        int indice = indice(chave);
        ArrayList<No<K, V>> balde = baldes[indice];
        for (No<K, V> e : balde) {
            if (Objects.equals(e.chave, chave)) {
                V valorAnterior = e.valor;
                e.valor = valor;
                return valorAnterior;
            }
        }

        if (tamanho + 1 > baldes.length * FATOR_CARGA_MAXIMO) {
            redimensionar();
            indice = indice(chave);
        }

        baldes[indice].add(new No<>(chave, valor));
        tamanho++;
        return null;
    }

    public V get(K chave) {
        for (No<K, V> e : baldes[indice(chave)]) {
            if (Objects.equals(e.chave, chave)) {
                return e.valor;
            }
        }
        return null;
    }

    public boolean containsKey(K chave) {
        for (No<K, V> e : baldes[indice(chave)]) {
            if (Objects.equals(e.chave, chave)) {
                return true;
            }
        }
        return false;
    }

    public V remove(K chave) {
        ArrayList<No<K, V>> balde = baldes[indice(chave)];
        for (int i = 0; i < balde.size(); i++) {
            No<K, V> e = balde.get(i);
            if (Objects.equals(e.chave, chave)) {
                balde.remove(i);
                tamanho--;
                return e.valor;
            }
        }
        return null;
    }

    public int size() {
        return tamanho;
    }

    public boolean isEmpty() {
        return tamanho == 0;
    }

    public void clear() {
        for (ArrayList<No<K, V>> balde : baldes) {
            balde.clear();
        }
        tamanho = 0;
    }

    public List<Entrada<K, V>> entradas() {
        List<Entrada<K, V>> resultado = new ArrayList<>(tamanho);
        for (ArrayList<No<K, V>> balde : baldes) {
            for (No<K, V> e : balde) {
                resultado.add(new Entrada<>(e.chave, e.valor));
            }
        }
        return resultado;
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        ArrayList<No<K, V>>[] baldesAntigos = baldes;
        baldes = (ArrayList<No<K, V>>[]) new ArrayList[baldesAntigos.length * 2];
        for (int i = 0; i < baldes.length; i++) {
            baldes[i] = new ArrayList<>();
        }

        for (ArrayList<No<K, V>> balde : baldesAntigos) {
            for (No<K, V> e : balde) {
                baldes[indice(e.chave)].add(e);
            }
        }
    }
}
