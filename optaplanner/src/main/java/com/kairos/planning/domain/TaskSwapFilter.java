package com.kairos.planning.domain;

import com.kairos.planning.solution.TaskPlanningSolution;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class TaskSwapFilter implements SelectionFilter<TaskPlanningSolution,SwapMove> {
   // @Override
    public boolean accept(ScoreDirector<TaskPlanningSolution> scoreDirector, SwapMove swapMove) {

            Task task1 = (Task)swapMove.getLeftEntity();
            Task task2 = (Task)swapMove.getRightEntity();
            Employee emp1= task1.getEmployee();
            Employee emp2= task2.getEmployee();
            if(!emp1.canAttemptedToPlanIhisInterval(task2.getPossibleStartInterval())){
                return false;
            }
            if(!emp2.canAttemptedToPlanIhisInterval(task1.getPossibleStartInterval())){
                return false;
            }


        return true;
    }

}
