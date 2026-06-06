import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 智能异常文件&目录检测器
 * 1. 最多扫描6层目录
 * 2. 检测：空文件、乱码文件名、同级目录文件数量不一致
 * 3. 控制台输出异常路径与问题类型
 */
public class SmartAbnormalChecker {
    // 最大扫描层数
    private static final int MAX_LEVEL = 6;
    // 合法文件名正则（过滤乱码、不可见字符、特殊非法字符）
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[^\\x00-\\x1F\\\\/:*?\"<>|]+$");
    // 记录每一层 同级文件夹 → 文件数量，用于校验数量一致性
    private static final Map<Integer, Map<String, Integer>> levelDirFileCount = new HashMap<>();

    public static void main(String[] args) {
        File root = new File("C:\\Users\\zhangyadong\\Desktop\\检查资料"); // 扫描【当前项目根目录】
        System.out.println("===== 智能异常文件/目录检测工具（最多6层） =====");
        System.out.println("开始扫描路径：" + root.getAbsolutePath() + "\n");

        scanDir(root, 0);

        // 校验所有层级【同级目录文件数量是否一致】
        checkDirCountConsistency();

        System.out.println("\n===== 扫描结束 =====");
    }

    /**
     * 递归扫描目录
     * @param dir 当前目录
     * @param currentLevel 当前层级（0=根目录）
     */
    private static void scanDir(File dir, int currentLevel) {
        if (currentLevel >= MAX_LEVEL) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            System.out.println("【异常】权限不足/无法读取目录：" + dir.getAbsolutePath());
            return;
        }

        // 统计当前文件夹内【文件数量】（不含子文件夹）
        int fileCount = 0;
        List<File> subDirList = new ArrayList<>();

        for (File f : files) {
            if (f.isFile()) {
                fileCount++;
                // 1. 检测空文件（0字节，表格/文档无内容）
                if (f.length() == 0) {
                    System.out.println("⚠️ 【空文件异常】" + f.getAbsolutePath() + " —— 文件无内容");
                }
                // 2. 检测乱码/非法文件名
                if (!VALID_NAME_PATTERN.matcher(f.getName()).matches()) {
                    System.out.println("⚠️ 【文件名乱码异常】" + f.getAbsolutePath() + " —— 包含非法/不可见字符");
                }
            } else if (f.isDirectory()) {
                subDirList.add(f);
                // 记录【层级-目录名-文件数量】，用于后续数量校验
                levelDirFileCount
                        .computeIfAbsent(currentLevel + 1, k -> new HashMap<>())
                        .put(f.getAbsolutePath(), getFileCount(f));
            }
        }

        // 递归扫描子目录
        for (File subDir : subDirList) {
            scanDir(subDir, currentLevel + 1);
        }
    }

    /**
     * 获取一个文件夹内【文件数量】（不含子文件夹）
     */
    private static int getFileCount(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            if (f.isFile()) count++;
        }
        return count;
    }

    /**
     * 智能校验：同一层级下，同级文件夹的文件数量是否一致
     * 例：阶段1有3个文档，阶段2只有2个 → 判定异常
     */
    private static void checkDirCountConsistency() {
        for (Map.Entry<Integer, Map<String, Integer>> entry : levelDirFileCount.entrySet()) {
            int level = entry.getKey();
            Map<String, Integer> dirCountMap = entry.getValue();
            if (dirCountMap.size() < 2) continue; // 只有1个目录，无需校验

            // 取第一个目录的文件数量作为标准
            int standardCount = dirCountMap.values().iterator().next();
            boolean hasAbnormal = false;

            for (Map.Entry<String, Integer> dirEntry : dirCountMap.entrySet()) {
                String dirPath = dirEntry.getKey();
                int count = dirEntry.getValue();
                if (count != standardCount) {
                    System.out.println("⚠️ 【同级文件数量异常】层级" + level + " —— " + dirPath
                            + " 内文件数：" + count + "，标准数量：" + standardCount);
                    hasAbnormal = true;
                }
            }
        }
    }
}