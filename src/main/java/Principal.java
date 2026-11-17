public class Principal {

    public static void main(String[] args) {
        MeuMapa<String, String> mapa = new MeuMapa<>();

        System.out.println("inserção e consulta");
        mapa.put("Brasil", "Brasília");
        mapa.put("Argentina", "Buenos Aires");
        mapa.put("Chile", "Santiago");
        System.out.println("Brasil: " + mapa.get("Brasil"));
        System.out.println("Argentina: " + mapa.get("Argentina"));

        System.out.println("\natualização de chave existente");
        System.out.println("Chile antes: " + mapa.get("Chile"));
        mapa.put("Chile", "Valparaíso");
        System.out.println("Chile depois: " + mapa.get("Chile"));

        System.out.println("\nchave não cadastrada");
        System.out.println("Peru: " + mapa.get("Peru"));

        System.out.println("\ncolisão: \"Aa\" e \"BB\" têm o mesmo hashCode");
        System.out.println("hashCode(Aa) = " + "Aa".hashCode() + " | hashCode(BB) = " + "BB".hashCode());
        mapa.put("Aa", "valor 1");
        mapa.put("BB", "valor 2");
        System.out.println("Aa: " + mapa.get("Aa"));
        System.out.println("BB: " + mapa.get("BB"));

        System.out.println("\ngenéricos: chave Integer, valor String");
        MeuMapa<Integer, String> numeros = new MeuMapa<>();
        numeros.put(7, "sete");
        System.out.println("7: " + numeros.get(7));
    }
}
