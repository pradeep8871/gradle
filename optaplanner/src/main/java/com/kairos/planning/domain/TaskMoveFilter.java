package com.kairos.planning.domain;

import com.kairos.planning.solution.TaskPlanningSolution;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

//TODO This is , as of now, for both CH and MH and should have separate for both as CH mostly have unitialized PVs
public class TaskMoveFilter implements SelectionFilter<TaskPlanningSolution,ChangeMove> {

   // @Override
    public boolean accept(ScoreDirector<TaskPlanningSolution> scoreDirector, ChangeMove changeMove) {
             Task task = (Task)changeMove.getEntity();
            if(task.getPreviousTaskOrEmployee()==null){
                //Returning as it's unitialized in CH phases. Note that this should never be called in Mh as it has fully initialized solution
                return false;
            }
            //prevTaskOrVehicle is the valve on which this filter works
            TaskOrEmployee prevTaskOrEmployee = (TaskOrEmployee)changeMove.getToPlanningValue();
            Vehicle vehicle= task.getVehicle();
            if(vehicle==null){
                //Returning as it's unitialized in CH phases. Note that this should never be called in Mh as it has fully initialized solution
                return false;
            }
            if(prevTaskOrEmployee instanceof  Task){
                Task prevTask = (Task) prevTaskOrEmployee;
                Employee employee=prevTask.getEmployee();//(Employee)changeMove.getToPlanningValue();//task.getEmployee();
                if(employee==null){
                    //Returning as it's unitialized in CH phases. Note that this should never be called in Mh as it has fully initialized solution
                    return false;
                }
                int missingSkills=prevTask.getMissingSkillCountForEmployee(employee);
                if(missingSkills>0){
                    return false;
                }
                if(!prevTask.canEmployeeWork(employee)){
                    return false;
                }
                if(!prevTask.canEmployeeReachBack(employee)){
                    return false;
                }
                if(!employee.canAttemptedToPlanIhisInterval(task.getPossibleStartInterval())){
                    return false;
                }
                if(!task.isAfter(prevTask)) return false;
            }


        return true;
    }

}
