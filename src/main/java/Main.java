public class Main {

    public static void main(String[] args) {

        Promise<Integer> p = new Promise<>(() -> {
            Thread.sleep(5000);
            return maisUm(0);
        });


        p = p.success(Main::maisUm);

        p = p.success(Main::maisUm);

        p = p.success(Main::maisUm);

        p = p.success(Main::maisUm);

        System.out.println(-1);

    }

    private static int maisUm(int i) {
        System.out.println(i);
        return i + 1;
    }
}
