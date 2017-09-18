package com.kairos.planning.domain;

import com.kairos.planning.executioner.TaskPlanningSolver;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class StartTimeVariableListener implements VariableListener<Task> {
    public static Logger log= LoggerFactory.getLogger(TaskPlanningSolver.class);

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Task task) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Task task) {

    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Task task) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Task task) {
        updateStartTime(scoreDirector,task);
    }



    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Task task) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Task task) {

    }
    private void updateStartTime(ScoreDirector scoreDirector, Task task) {


        if(task.getPreviousTaskOrEmployee() == null){
            scoreDirector.beforeVariableChanged(task,"plannedStartTime");
            task.setPlannedStartTime(task.getInitialStartTime());
            scoreDirector.afterVariableChanged(task,"plannedStartTime");
            //log.error("Returning with setting initial start as planned start time for task:"+task);
            return;
        }
        DateTime plannedStartTime=getPossibleStartTime(task);
        while(task!=null && !Objects.equals(task.getPlannedStartTime(),plannedStartTime)){
            scoreDirector.beforeVariableChanged(task,"plannedStartTime");
            task.setPlannedStartTime(plannedStartTime);
            scoreDirector.afterVariableChanged(task,"plannedStartTime");
            task=task.getNextTask();
            if(task==null) break;
            plannedStartTime=getPossibleStartTime(task);
        }
    }
    private DateTime getPossibleStartTime(Task task){
        DateTime initialReachingTime= task.getPreviousTaskOrEmployee() instanceof Task ? ((Task) task.getPreviousTaskOrEmployee()).getPlannedEndTime().plusMinutes(task.getDrivingMinutesFromPreviousTaskOrEmployee())
                :task.getInitialStartTime().minusMinutes(task.getDrivingMinutesFromPreviousTaskOrEmployee());
        //DateTime earliestStart= task.getEarliestStartTime();
        Interval possibleTaskInterval = task.getPossibleStartInterval();
        DateTime plannedStartTime= null;//possibleTaskInterval.contains(initialReachingTime)?initialReachingTime:;
        //This considers earliest and latest start time
        if(possibleTaskInterval.getStart().equals(initialReachingTime) || possibleTaskInterval.contains(initialReachingTime) || possibleTaskInterval.getEnd().isEqual(initialReachingTime)){
            plannedStartTime=initialReachingTime;
        }else  if(possibleTaskInterval.isAfter(initialReachingTime)){
            plannedStartTime=task.getEarliestStartTime();
        }else{
           // log.info("No point for this because employee will reach after latest start time of task");
            plannedStartTime=task.getInitialStartTime();
        }
        return plannedStartTime;
        //return task.getReachingTime().plusMinutes(task.getWaitingMinutes());
    }
}
