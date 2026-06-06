import java.io.File;
import java.util.Scanner;

/**
 * 文件夹层级遍历（最多6层）
 * 兼容 Java 8，树形美观排版
 */
public class DirTreePrinter {

    // 最大遍历 6 层
    private static final int MAX_LEVEL = 7;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入要遍历的文件夹路径：");
        String rootPath = scanner.nextLine().trim();
        scanner.close();

        File root = new File(rootPath);

        if (!root.exists()) {
            System.out.println("❌ 路径不存在！");
            return;
        }
        if (!root.isDirectory()) {
            System.out.println("❌ 输入的不是文件夹！");
            return;
        }

        System.out.println("\n==================== 目录结构（最多6层） ====================\n");
        System.out.println(root.getAbsolutePath());

        // 开始打印，从第1层开始
        printTree(root, 1);

        System.out.println("\n============================================================");
        System.out.println("✅ 遍历完成，最大限制：6层");
    }

    /**
     * 递归打印树形结构
     */
    private static void printTree(File file, int level) {
        // 超过6层立刻停止
        if (level > MAX_LEVEL) {
            return;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        // 生成缩进（兼容 Java 8）
        String indent = getIndent(level);

        for (File f : files) {
            if (f.isDirectory()) {
                System.out.println(indent + "📂 【" + f.getName() + "】");
                // 递归进入下一层
                printTree(f, level + 1);
            } else {
                System.out.println(indent + "📄 " + f.getName());
            }
        }
    }

    /**
     * 生成层级缩进（兼容所有 Java 版本）
     */
    private static String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("    "); // 每层4个空格
        }
        return sb.toString();
    }
}