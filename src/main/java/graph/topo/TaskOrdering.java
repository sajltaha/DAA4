package graph.topo;

import java.util.*;

/**
 * Derives a task ordering from component ordering.
 * Maps the topological order of SCCs to original task vertices.
 */
public class TaskOrdering {

    private final List<Integer> componentOrder;
    private final List<List<Integer>> sccs;
    private List<Integer> taskOrder;

    public TaskOrdering(List<Integer> componentOrder, List<List<Integer>> sccs) {
        this.componentOrder = componentOrder;
        this.sccs = sccs;
    }

    /**
     * Derive task ordering from component ordering.
     * Tasks within the same SCC can be in any order (we use sorted order).
     * @return ordered list of original task vertices
     */
    public List<Integer> deriveTaskOrder() {
        taskOrder = new ArrayList<>();

        // Process components in topological order
        for (int compId : componentOrder) {
            List<Integer> component = sccs.get(compId);

            // Add all tasks from this component
            // Tasks within an SCC can be in any order (they form a cycle)
            // We use the natural order for consistency
            List<Integer> sortedTasks = new ArrayList<>(component);
            Collections.sort(sortedTasks);
            taskOrder.addAll(sortedTasks);
        }

        return taskOrder;
    }

    /**
     * Get the derived task order.
     * @return task order
     */
    public List<Integer> getTaskOrder() {
        if (taskOrder == null) {
            throw new IllegalStateException("Must call deriveTaskOrder() first");
        }
        return taskOrder;
    }

    /**
     * Print task ordering information.
     */
    public void printTaskOrder() {
        System.out.println("\n=== Task Ordering (Derived from Component Order) ===");
        System.out.println("Total tasks: " + taskOrder.size());
        System.out.println("\nTask execution order:");

        int position = 0;
        for (int compId : componentOrder) {
            List<Integer> component = sccs.get(compId);
            System.out.printf("Step %d - Component %d (SCC with %d tasks): ",
                    position + 1, compId, component.size());

            List<Integer> sortedTasks = new ArrayList<>(component);
            Collections.sort(sortedTasks);
            System.out.println(sortedTasks);

            position++;
        }

        System.out.println("\nFull task sequence: " + taskOrder);
    }

    /**
     * Get ordering by groups (each SCC is a group).
     * @return list of task groups in execution order
     */
    public List<List<Integer>> getGroupedOrder() {
        List<List<Integer>> groups = new ArrayList<>();

        for (int compId : componentOrder) {
            List<Integer> component = new ArrayList<>(sccs.get(compId));
            Collections.sort(component);
            groups.add(component);
        }

        return groups;
    }

    /**
     * Get a map from task to its execution phase.
     * All tasks in the same SCC have the same phase.
     * @return map of task -> phase number
     */
    public Map<Integer, Integer> getTaskPhases() {
        Map<Integer, Integer> phases = new HashMap<>();

        for (int phase = 0; phase < componentOrder.size(); phase++) {
            int compId = componentOrder.get(phase);
            for (int task : sccs.get(compId)) {
                phases.put(task, phase);
            }
        }

        return phases;
    }
}