package com.kairos.planning.domain;

import com.kairos.planning.solution.TaskPlanningSolution;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * This is , as of now, for  only CH
 */

public class TaskMoveCHFilter implements SelectionFilter<TaskPlanningSolution,ChangeMove> {

   // @Override
    public boolean accept(ScoreDirector<TaskPlanningSolution> scoreDirector, ChangeMove changeMove) {
             Task task = (Task)changeMove.getEntity();
             TaskOrEmployee prevTaskOrEmployee = (TaskOrEmployee)changeMove.getToPlanningValue();
             if(task==null || prevTaskOrEmployee ==null ||  prevTaskOrEmployee instanceof  Employee) return true;
            if(!task.getInitialInterval().getStart().isAfter(((Task) prevTaskOrEmployee).getInitialInterval().getEnd())){
                return false;
            }
        return true;
    }

}
