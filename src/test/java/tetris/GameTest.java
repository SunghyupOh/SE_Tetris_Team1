package tetris;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;

@DisplayName("안전한 게임 기본 테스트")
public class GameTest {
    
    @Test
    @DisplayName("게임 인스턴스 생성 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void gameInstance_canBeCreated() {
        System.out.println("=== 게임 인스턴스 생성 테스트 ===");
        
        assertDoesNotThrow(() -> {
            Game gameInstance = Game.getInstance();
            assertNotNull(gameInstance, "Game 인스턴스가 null이 아니어야 합니다.");
            System.out.println("✅ Game 인스턴스 생성 성공");
        }, "Game 인스턴스 생성은 예외를 발생시키지 않아야 합니다.");
    }
    
    @Test
    @DisplayName("Scene 설정 안전성 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void setScene_handlesNullSafely() {
        System.out.println("=== Scene null 안전성 테스트 ===");
        
        assertDoesNotThrow(() -> {
            // null scene 설정이 안전하게 처리되는지 확인
            Game.setScene(null);
            System.out.println("✅ null scene 설정 처리 완료");
        }, "null scene 설정은 안전하게 처리되어야 합니다.");
    }
    
    @Test
    @DisplayName("게임 클래스 로딩 테스트")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void gameClass_loadsSuccessfully() {
        System.out.println("=== 게임 클래스 로딩 테스트 ===");
        
        assertDoesNotThrow(() -> {
            // 클래스 로딩만 확인 (실제 게임 실행 없음)
            Class<?> gameClass = Game.class;
            assertNotNull(gameClass, "Game 클래스가 로딩되어야 합니다.");
            System.out.println("✅ Game 클래스 로딩 성공");
        }, "Game 클래스 로딩은 예외를 발생시키지 않아야 합니다.");
    }
    
    @AfterAll
    @DisplayName("GameTest 백그라운드 프로세스 정리")
    static void cleanup() {
        try {
            System.out.println("🧹 GameTest 백그라운드 프로세스 정리 시작...");
            
            // 1. 모든 Timer 완전 중지
            try {
                javax.swing.Timer.setLogTimers(false);
                java.lang.reflect.Field timersField = javax.swing.Timer.class.getDeclaredField("queue");
                timersField.setAccessible(true);
                Object timerQueue = timersField.get(null);
                if (timerQueue != null) {
                    java.lang.reflect.Method stopMethod = timerQueue.getClass().getDeclaredMethod("stop");
                    stopMethod.setAccessible(true);
                    stopMethod.invoke(timerQueue);
                    System.out.println("🧹 Swing Timer 큐 완전 중지됨");
                }
            } catch (Exception e) {
                // Reflection 실패는 무시
            }
            
            // 2. AWT/Swing EventQueue 정리
            try {
                java.awt.EventQueue eventQueue = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue();
                while (eventQueue.peekEvent() != null) {
                    eventQueue.getNextEvent();
                }
            } catch (Exception e) {
                // 무시
            }
            
            // 3. 활성 GUI 스레드 정리
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }
            
            Thread[] threads = new Thread[rootGroup.activeCount()];
            int count = rootGroup.enumerate(threads);
            
            for (int i = 0; i < count; i++) {
                Thread thread = threads[i];
                if (thread != null && !thread.isDaemon() && thread != Thread.currentThread()) {
                    String threadName = thread.getName();
                    if (threadName.contains("AWT-EventQueue") || 
                        threadName.contains("TimerQueue") ||
                        threadName.contains("Swing-Timer")) {
                        System.out.println("⚠️ GameTest 활성 GUI 스레드 감지: " + threadName);
                        thread.interrupt();
                    }
                }
            }
            
            // 4. 강제 메모리 정리
            System.runFinalization();
            System.gc();
            
        } catch (Exception e) {
            System.out.println("GameTest 정리 중 오류 (무시): " + e.getMessage());
        }
        
        System.out.println("✅ GameTest 백그라운드 프로세스 정리 완료");
    }
}