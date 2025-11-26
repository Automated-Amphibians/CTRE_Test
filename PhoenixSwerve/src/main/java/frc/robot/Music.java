// package frc.robot;

// import java.io.*;
// import java.nio.file.*;
// import java.util.*;
// import java.util.concurrent.*;
// import java.util.stream.*;

// /**
//  * Single-file "orchestra" that maps CAN motor ids to Motor objects,
//  * loads a simple song file, and plays it by scheduling start/stop events.
//  *
//  * Test/main will create a sample song and test with one CAN motor (id 1).
//  */
// public class Music {

//     // --- Simple Motor abstraction (replace sendCanMessage with real API calls) ---
//     public static class Motor {
//         private final int canId;
//         private volatile double currentPower = 0.0;

//         public Motor(int canId) {
//             this.canId = canId;
//         }

//         public int getCanId() {
//             return canId;
//         }

//         // Simulate setting power; in real robot code replace with CAN library call
//         public void setPower(double power) {
//             currentPower = power;
//             // Replace this println with actual CAN command, e.g., talon.set(power) etc.
//             System.out.printf("[Motor %d] setPower(%.3f) at %dms%n", canId, power, System.currentTimeMillis()%100000);
//         }

//         public double getCurrentPower() {
//             return currentPower;
//         }
//     }

//     // --- Orchestra manages motors by CAN id ---
//     public static class Orchestra {
//         private final Map<Integer, Motor> motors = new ConcurrentHashMap<>();

//         public Motor addMotor(int canId) {
//             return motors.computeIfAbsent(canId, Motor::new);
//         }

//         public Optional<Motor> getMotor(int canId) {
//             return Optional.ofNullable(motors.get(canId));
//         }

//         public Collection<Motor> allMotors() {
//             return motors.values();
//         }
//     }

//     // --- Events and Song ---
//     public static class NoteEvent {
//         public final long timeMs;      // when to start (offset from song start in ms)
//         public final int canId;
//         public final long durationMs;  // how long to play
//         public final double power;     // power -1.0 .. 1.0

//         public NoteEvent(long timeMs, int canId, long durationMs, double power) {
//             this.timeMs = timeMs;
//             this.canId = canId;
//             this.durationMs = durationMs;
//             this.power = power;
//         }

//         public long getEndTimeMs() {
//             return timeMs + durationMs;
//         }

//         @Override
//         public String toString() {
//             return String.format("NoteEvent(time=%dms, id=%d, dur=%dms, p=%.3f)", timeMs, canId, durationMs, power);
//         }
//     }

//     public static class Song {
//         public final List<NoteEvent> events;

//         public Song(List<NoteEvent> events) {
//             this.events = Collections.unmodifiableList(new ArrayList<>(events));
//         }

//         public long lengthMs() {
//             return events.stream().mapToLong(NoteEvent::getEndTimeMs).max().orElse(0);
//         }

//         public static Song loadFromFile(Path path) throws IOException {
//             List<NoteEvent> list = new ArrayList<>();
//             try (Stream<String> lines = Files.lines(path)) {
//                 int ln = 0;
//                 for (Iterator<String> it = lines.iterator(); it.hasNext(); ) {
//                     String raw = it.next();
//                     ln++;
//                     String line = raw.trim();
//                     if (line.isEmpty() || line.startsWith("#")) continue;
//                     // expected: time_ms canId duration_ms power
//                     String[] parts = line.split("\\s+");
//                     if (parts.length < 4) {
//                         System.err.printf("Skipping malformed line %d: %s%n", ln, raw);
//                         continue;
//                     }
//                     try {
//                         long timeMs = Long.parseLong(parts[0]);
//                         int canId = Integer.parseInt(parts[1]);
//                         long durMs = Long.parseLong(parts[2]);
//                         double power = Double.parseDouble(parts[3]);
//                         list.add(new NoteEvent(timeMs, canId, durMs, power));
//                     } catch (NumberFormatException e) {
//                         System.err.printf("Skipping invalid numbers on line %d: %s%n", ln, raw);
//                     }
//                 }
//             }
//             // sort by time
//             list.sort(Comparator.comparingLong(e -> e.timeMs));
//             return new Song(list);
//         }
//     }

//     // --- Player schedules events relative to start ---
//     public static class Player {
//         private final Orchestra orchestra;
//         private final ScheduledExecutorService scheduler;

//         public Player(Orchestra orchestra) {
//             this.orchestra = orchestra;
//             this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
//                 Thread t = new Thread(r, "song-player");
//                 t.setDaemon(false);
//                 return t;
//             });
//         }

//         /**
//          * Play a song. This method schedules start and stop tasks and returns a Future
//          * that completes when playback finishes.
//          */
//         public CompletableFuture<Void> play(Song song) {
//             long songLength = song.lengthMs();
//             long startTime = System.currentTimeMillis();

//             CompletableFuture<Void> done = new CompletableFuture<>();
//             if (song.events.isEmpty()) {
//                 done.complete(null);
//                 return done;
//             }

//             for (NoteEvent e : song.events) {
//                 if (e.timeMs < 0 || e.durationMs < 0) {
//                     System.err.println("Ignoring event with negative time/duration: " + e);
//                     continue;
//                 }
//                 // schedule start
//                 long delayStart = e.timeMs - (System.currentTimeMillis() - startTime);
//                 if (delayStart < 0) delayStart = 0;
//                 scheduler.schedule(() -> {
//                     Motor m = orchestra.addMotor(e.canId); // ensure motor exists
//                     // start
//                     System.out.printf("[Player] START %s (scheduled @%dms)%n", e, e.timeMs);
//                     m.setPower(e.power);
//                 }, delayStart, TimeUnit.MILLISECONDS);

//                 // schedule stop at time + duration
//                 long delayStop = (e.timeMs + e.durationMs) - (System.currentTimeMillis() - startTime);
//                 if (delayStop < 0) delayStop = 0;
//                 scheduler.schedule(() -> {
//                     Motor m = orchestra.getMotor(e.canId).orElse(null);
//                     System.out.printf("[Player] STOP  %s (scheduled end @%dms)%n", e, e.getEndTimeMs());
//                     if (m != null) m.setPower(0.0);
//                 }, delayStop, TimeUnit.MILLISECONDS);
//             }

//             // schedule a completion task after songLength + small buffer
//             scheduler.schedule(() -> {
//                 // safety: ensure all motors stopped
//                 orchestra.allMotors().forEach(m -> {
//                     if (m.getCurrentPower() != 0.0) {
//                         System.out.printf("[Player] Final stop of motor %d%n", m.getCanId());
//                         m.setPower(0.0);
//                     }
//                 });
//                 // shutdown scheduler
//                 scheduler.shutdown();
//                 done.complete(null);
//             }, songLength + 200, TimeUnit.MILLISECONDS);

//             return done;
//         }
//     }

//     // --- Utility to create a sample song file for demo ---
//     private static Path createSampleSongFile() throws IOException {
//         Path tmp = Files.createTempFile("sample-song-", ".txt");
//         List<String> lines = Arrays.asList(
//             "# sample song: time_ms canId duration_ms power",
//             "# This will run motor 1 for 500ms at power 0.8 at t=0, then reverse at 1000ms",
//             "0 1 500 0.8",
//             "1000 1 700 -0.6",
//             "2000 1 300 0.5",
//             "2600 1 200 0.0"
//         );
//         Files.write(tmp, lines, StandardOpenOption.TRUNCATE_EXISTING);
//         System.out.println("Wrote sample song to: " + tmp.toAbsolutePath());
//         return tmp;
//     }
// }
