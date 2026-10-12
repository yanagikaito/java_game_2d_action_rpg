import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.classfile.*;
import java.lang.classfile.instruction.InvokeInstruction;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionRpgBugCheckerTest {

    @Test
    public void testArchitectureAndBugs() throws IOException {

        Path classesDir = Paths.get("build/classes/java/main");

        if (!Files.exists(classesDir)) {
            return;
        }

        boolean hasViolation = false;

        // ディレクトリの中を再帰的にすべて歩き回る（Walkする）
        try (Stream<Path> paths = Files.walk(classesDir)) {
            for (Path classFile : (Iterable<Path>) paths::iterator) {
                if (classFile.toString().endsWith(".class")) {
                    // 見つかった「すべてのクラスファイル」を1つずつチェックに掛けている
                    boolean failed = checkClass(classFile);
                    if (failed) {
                        hasViolation = true;
                    }
                }
            }
        }

        assertTrue(!hasViolation, "[ActionRPG] アーキテクチャ違反や禁止された実装パターンが検出されました！");
    }

    private boolean checkClass(Path classFile) {

        try {
            ClassModel classModel = ClassFile.of().parse(classFile);
            String className = classModel.thisClass().name().stringValue();

            final boolean[] classHasError = {false};

            for (MethodModel method : classModel.methods()) {
                String methodName = method.methodName().stringValue();

                method.code().ifPresent(code -> {
                    for (CodeElement element : code) {
                        if (element instanceof InvokeInstruction invoke) {
                            String owner = invoke.owner().name().stringValue();
                            String name = invoke.name().stringValue();

                            // 例: 廃止した古いロード処理を誤って呼び出していないかチェック
                            if (owner.contains("OldLoadManager") && name.equals("deprecatedLoad")) {
                                System.err.println("[検出: 廃止メソッド] クラス: " + className + " # " + methodName);
                                classHasError[0] = true;
                            }

                            // owner がモンスター系クラスであり、かつメソッド名がコンストラクタ（"<init>"）の場合
                            if (owner.contains("MonGreenSlime") && name.equals("<init>")) {

                                // 例外: EntityFactory 自身の中での new は許可する
                                if (!className.contains("EntityFactory")) {
                                    System.err.println("[アーキテクチャ違反] " + className + " がファクトリーを経由せずに直接モンスターを生成しています！");
                                    classHasError[0] = true;
                                }
                            }

                            if (owner.contains("MonMintSoldier") && name.equals("<init>")) {

                                if (!className.contains("EntityFactory")) {
                                    System.err.println("[アーキテクチャ違反] " + className + " がファクトリーを経由せずに直接モンスターを生成しています！");
                                    classHasError[0] = true;
                                }
                            }

                            if (owner.contains("MonGreenGoblin") && name.equals("<init>")) {

                                if (!className.contains("EntityFactory")) {
                                    System.err.println("[アーキテクチャ違反] " + className + " がファクトリーを経由せずに直接モンスターを生成しています！");
                                    classHasError[0] = true;
                                }
                            }

                            if (owner.contains("NpcChicken") && name.equals("<init>")) {

                                if (!className.contains("EntityFactory")) {
                                    System.err.println("[アーキテクチャ違反] " + className + " がファクトリーを経由せずに直接モンスターを生成しています！");
                                    classHasError[0] = true;
                                }
                            }
                        }
                    }
                });
            }
            return classHasError[0];
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}