package frame;

import javax.swing.*;

@FunctionalInterface
public interface GameFrame {
    /**
     * フレームを作成して返す。
     * 実装側は JFrame を生成して返すか、必要なら表示まで行う。
     */
    JFrame create();
}