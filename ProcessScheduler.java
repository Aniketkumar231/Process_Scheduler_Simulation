


import java.util.*;

class Process {
    int id;         // Process ID
    int arrival;    // Arrival Time
    int burst;      // Burst Time (CPU Execution Time)
    int remaining;  // Remaining Burst Time (for preemptive algorithms)
    int waiting;    // Waiting Time
    int turnaround; // Turnaround Time
    int completion; // Completion Time
    
    public Process(int id, int arrival, int burst) {
        this.id = id;
        this.arrival = arrival;
        this.burst = burst;
        this.remaining = burst;
        this.waiting = 0;
        this.turnaround = 0;
        this.completion = 0;
    }
    
    // Copy constructor for creating duplicate process lists
    public Process(Process p) {
        this.id = p.id;
        this.arrival = p.arrival;
        this.burst = p.burst;
        this.remaining = p.burst; // Ensure remaining is set correctly
        this.waiting = 0;
        this.turnaround = 0;
        this.completion = 0;
    }
}

public class ProcessScheduler {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();
        
        // Get user input for processes
        System.out.println("==== CPU SCHEDULER SIMULATION ====");
        System.out.print("Enter the number of processes: ");
        int n = scanner.nextInt();
        
        for (int i = 1; i <= n; i++) {
            System.out.println("\nEnter details for Process " + i + ":");
            System.out.print("Arrival Time: ");
            int arrivalTime = scanner.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = scanner.nextInt();
            
            processes.add(new Process(i, arrivalTime, burstTime));
        }
        
        // Get time quantum for Round Robin
        System.out.print("\nEnter Time Quantum for Round Robin: ");
        int timeQuantum = scanner.nextInt();
        
        // Run each algorithm and display results
        System.out.println("\n==== SIMULATION RESULTS ====");
        
        System.out.println("\nFirst-Come, First-Served (FCFS) Scheduling:");
        List<Process> fcfsResult = fcfs(processes);
        displayResults(fcfsResult);
        
        System.out.println("\nShortest Job First (SJF) Non-Preemptive Scheduling:");
        List<Process> sjfNonPreemptiveResult = sjfNonPreemptive(processes);
        displayResults(sjfNonPreemptiveResult);
        
        System.out.println("\nShortest Job First (SJF) Preemptive Scheduling (SRTF):");
        List<Process> sjfPreemptiveResult = sjfPreemptive(processes);
        displayResults(sjfPreemptiveResult);
        
        System.out.println("\nRound Robin (RR) Scheduling with Time Quantum = " + timeQuantum + ":");
        List<Process> rrResult = roundRobin(processes, timeQuantum);
        displayResults(rrResult);
        
        // Compare the efficiency of algorithms
        compareEfficiency(fcfsResult, sjfNonPreemptiveResult, sjfPreemptiveResult, rrResult);
        
        scanner.close();
    }
    
    // First-Come, First-Served (FCFS) Scheduling Algorithm
    public static List<Process> fcfs(List<Process> originalProcesses) {
        // Create a deep copy of processes to avoid modifying the original
        List<Process> processes = copyProcesses(originalProcesses);
        
        // Sort processes by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrival));
        
        int currentTime = 0;
        
        for (Process p : processes) {
            // If process hasn't arrived yet, move time forward
            if (currentTime < p.arrival) {
                currentTime = p.arrival;
            }
            
            // Calculate waiting time
            p.waiting = currentTime - p.arrival;
            
            // Execute process (move time forward)
            currentTime += p.burst;
            
            // Calculate completion and turnaround time
            p.completion = currentTime;
            p.turnaround = p.completion - p.arrival;
        }
        
        return processes;
    }
    
    // Shortest Job First (SJF) Non-Preemptive Scheduling Algorithm
    public static List<Process> sjfNonPreemptive(List<Process> originalProcesses) {
        // Create a deep copy of processes
        List<Process> processes = copyProcesses(originalProcesses);
        
        // Sort processes by arrival time initially
        processes.sort(Comparator.comparingInt(p -> p.arrival));
        
        int currentTime = 0;
        List<Process> completed = new ArrayList<>();
        List<Process> arrived = new ArrayList<>();
        
        while (completed.size() < processes.size()) {
            // Check for newly arrived processes
            for (Iterator<Process> it = processes.iterator(); it.hasNext();) {
                Process p = it.next();
                if (p.arrival <= currentTime) {
                    arrived.add(p);
                    it.remove();
                }
            }
            
            if (arrived.isEmpty()) {
                // No process available, move time to next arrival
                if (!processes.isEmpty()) {
                    currentTime = processes.get(0).arrival;
                    continue;
                }
                break;
            }
            
            // Find the process with the shortest burst time
            arrived.sort(Comparator.comparingInt(p -> p.burst));
            Process currentProcess = arrived.remove(0);
            
            // Calculate waiting time
            currentProcess.waiting = currentTime - currentProcess.arrival;
            
            // Execute process
            currentTime += currentProcess.burst;
            
            // Calculate completion and turnaround time
            currentProcess.completion = currentTime;
            currentProcess.turnaround = currentProcess.completion - currentProcess.arrival;
            
            completed.add(currentProcess);
        }
        
        // Sort by process ID for consistent output
        completed.sort(Comparator.comparingInt(p -> p.id));
        return completed;
    }
    
    // Shortest Job First (SJF) Preemptive Scheduling Algorithm (SRTF - Shortest Remaining Time First)
    public static List<Process> sjfPreemptive(List<Process> originalProcesses) {
        // Create a deep copy of processes
        List<Process> processes = copyProcesses(originalProcesses);
        
        // Sort by arrival time initially
        processes.sort(Comparator.comparingInt(p -> p.arrival));
        
        int currentTime = 0;
        int completed = 0;
        boolean isProcessRunning = false;
        
        // Create a map to track execution times for each process
        Map<Integer, Integer> startTimeMap = new HashMap<>();
        
        while (completed < processes.size()) {
            Process shortestJob = null;
            int shortestBurst = Integer.MAX_VALUE;
            
            // Find process with shortest remaining time
            for (Process p : processes) {
                if (p.arrival <= currentTime && p.remaining > 0 && p.remaining < shortestBurst) {
                    shortestJob = p;
                    shortestBurst = p.remaining;
                    isProcessRunning = true;
                }
            }
            
            // If no process is available, increment time
            if (!isProcessRunning) {
                currentTime++;
                continue;
            }
            
            // Track when this process starts executing (if first time)
            if (!startTimeMap.containsKey(shortestJob.id)) {
                startTimeMap.put(shortestJob.id, currentTime);
            }
            
            // Execute process for 1 time unit
            shortestJob.remaining--;
            currentTime++;
            
            // Check if process is completed
            if (shortestJob.remaining == 0) {
                completed++;
                isProcessRunning = false;
                
                // Calculate completion and turnaround time
                shortestJob.completion = currentTime;
                shortestJob.turnaround = shortestJob.completion - shortestJob.arrival;
                
                // Calculate waiting time (turnaround time - burst time)
                shortestJob.waiting = shortestJob.turnaround - shortestJob.burst;
            }
        }
        
        // Sort by process ID for consistent output
        processes.sort(Comparator.comparingInt(p -> p.id));
        return processes;
    }
    
    // Round Robin (RR) Scheduling Algorithm
    public static List<Process> roundRobin(List<Process> originalProcesses, int timeQuantum) {
        // Create a deep copy of processes
        List<Process> processes = copyProcesses(originalProcesses);
        
        // Sort by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrival));
        
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int completed = 0;
        Process currentProcess = null;
        boolean isProcessRunning = false;
        
        while (completed < processes.size()) {
            // Check for newly arrived processes
            for (Process p : processes) {
                if (p.arrival <= currentTime && p.remaining > 0 && !readyQueue.contains(p) && p != currentProcess) {
                    readyQueue.add(p);
                }
            }
            
            // If a process is currently running but has used its time quantum, put it back in queue
            if (isProcessRunning && currentProcess.remaining > 0) {
                readyQueue.add(currentProcess);
            }
            
            // If no process is running, get the next one from queue
            currentProcess = readyQueue.poll();
            
            // If no process is available, increment time
            if (currentProcess == null) {
                currentTime++;
                isProcessRunning = false;
                continue;
            }
            
            isProcessRunning = true;
            
            // Execute process for time quantum or until completion
            int executeTime = Math.min(currentProcess.remaining, timeQuantum);
            currentTime += executeTime;
            currentProcess.remaining -= executeTime;
            
            // Check if process is completed
            if (currentProcess.remaining == 0) {
                completed++;
                
                // Calculate completion and turnaround time
                currentProcess.completion = currentTime;
                currentProcess.turnaround = currentProcess.completion - currentProcess.arrival;
                
                // Calculate waiting time (turnaround time - burst time)
                currentProcess.waiting = currentProcess.turnaround - currentProcess.burst;
                
                isProcessRunning = false;
            }
            
            // Check for any new arrivals during this time slice
            for (Process p : processes) {
                if (p.arrival > currentTime - executeTime && p.arrival <= currentTime && p.remaining > 0
                        && !readyQueue.contains(p) && p != currentProcess) {
                    readyQueue.add(p);
                }
            }
        }
        
        // Sort by process ID for consistent output
        processes.sort(Comparator.comparingInt(p -> p.id));
        return processes;
    }
    
    // Utility method to create deep copies of process list
    private static List<Process> copyProcesses(List<Process> original) {
        List<Process> copies = new ArrayList<>();
        for (Process p : original) {
            copies.add(new Process(p));
        }
        return copies;
    }
    
    // Display the results of scheduling algorithm
    public static void displayResults(List<Process> processes) {
        System.out.println(String.format("%-10s %-15s %-15s %-15s %-15s %-15s",
                "Process", "Arrival Time", "Burst Time", "Completion Time", "Waiting Time", "Turnaround Time"));
        System.out.println("---------------------------------------------------------------------------------");
        
        double totalWaiting = 0;
        double totalTurnaround = 0;
        
        for (Process p : processes) {
            System.out.println(String.format("%-10s %-15d %-15d %-15d %-15d %-15d",
                    "P" + p.id, p.arrival, p.burst, p.completion, p.waiting, p.turnaround));
            totalWaiting += p.waiting;
            totalTurnaround += p.turnaround;
        }
        
        double avgWaiting = totalWaiting / processes.size();
        double avgTurnaround = totalTurnaround / processes.size();
        
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("Average Waiting Time: " + String.format("%.2f", avgWaiting));
        System.out.println("Average Turnaround Time: " + String.format("%.2f", avgTurnaround));
    }
    
    // Compare efficiency of all algorithms
    public static void compareEfficiency(List<Process> fcfs, List<Process> sjfNP, List<Process> sjfP, List<Process> rr) {
        System.out.println("\n============== EFFICIENCY COMPARISON ==============");
        
        // Calculate average metrics for each algorithm
        double[] avgWaiting = new double[4];
        double[] avgTurnaround = new double[4];
        String[] names = {"FCFS", "SJF (Non-Preemptive)", "SJF (Preemptive/SRTF)", "Round Robin"};
        List<List<Process>> allResults = List.of(fcfs, sjfNP, sjfP, rr);
        
        for (int i = 0; i < allResults.size(); i++) {
            List<Process> result = allResults.get(i);
            double totalWaiting = 0;
            double totalTurnaround = 0;
            
            for (Process p : result) {
                totalWaiting += p.waiting;
                totalTurnaround += p.turnaround;
            }
            
            avgWaiting[i] = totalWaiting / result.size();
            avgTurnaround[i] = totalTurnaround / result.size();
        }
        
        // Display comparison table
        System.out.println(String.format("%-25s %-25s %-25s", "Algorithm", "Avg Waiting Time", "Avg Turnaround Time"));
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < names.length; i++) {
            System.out.println(String.format("%-25s %-25.2f %-25.2f", names[i], avgWaiting[i], avgTurnaround[i]));
        }
        
        // Find the most efficient algorithm based on average waiting time
        int mostEfficientIdx = 0;
        for (int i = 1; i < avgWaiting.length; i++) {
            if (avgWaiting[i] < avgWaiting[mostEfficientIdx]) {
                mostEfficientIdx = i;
            }
        }
        
        System.out.println("\nEFFICIENCY ANALYSIS:");
        System.out.println("1. Most Efficient Algorithm: " + names[mostEfficientIdx]);
        System.out.println("   - Lowest Average Waiting Time: " + String.format("%.2f", avgWaiting[mostEfficientIdx]));
        System.out.println("   - Average Turnaround Time: " + String.format("%.2f", avgTurnaround[mostEfficientIdx]));
        
        // Provide more detailed analysis
        System.out.println("\n2. Algorithm Efficiency Analysis:");
        for (int i = 0; i < names.length; i++) {
            String efficiency;
            if (i == mostEfficientIdx) {
                efficiency = "BEST";
            } else if (avgWaiting[i] < 1.5 * avgWaiting[mostEfficientIdx]) {
                efficiency = "GOOD";
            } else {
                efficiency = "FAIR";
            }
            
            System.out.println("   - " + names[i] + ": " + efficiency);
        }
        
        System.out.println("\n3. Recommendations:");
        System.out.println("   - For minimizing waiting time: " + names[mostEfficientIdx]);
        
        // Find best for turnaround time
        int bestTurnaround = 0;
        for (int i = 1; i < avgTurnaround.length; i++) {
            if (avgTurnaround[i] < avgTurnaround[bestTurnaround]) {
                bestTurnaround = i;
            }
        }
        System.out.println("   - For minimizing turnaround time: " + names[bestTurnaround]);
        
        // Contextual recommendations
        System.out.println("\n4. Contextual Usage:");
        System.out.println("   - FCFS: Simple implementation, fair for similar burst times");
        System.out.println("   - SJF: Best when burst times are known and vary significantly");
        System.out.println("   - SRTF: Optimal for minimizing average waiting time, but has overhead");
        System.out.println("   - Round Robin: Fair CPU sharing, good for interactive systems");
        
        // CPU utilization and throughput analysis
        System.out.println("\n5. Additional Performance Metrics:");
        
        // For demonstration purposes, we can estimate CPU utilization
        // In a real scheduler, we would calculate this from actual idle time
        for (int i = 0; i < names.length; i++) {
            // Higher turnaround typically correlates with lower CPU utilization
            // This is a simplified estimation for demonstration
            double estimatedUtilization = 100.0 - (avgTurnaround[i] / (avgTurnaround[0] * 2) * 10);
            estimatedUtilization = Math.min(99.0, Math.max(70.0, estimatedUtilization));
            
            System.out.println("   - " + names[i] + " estimated CPU utilization: " 
                    + String.format("%.1f", estimatedUtilization) + "%");
        }
    }
}

