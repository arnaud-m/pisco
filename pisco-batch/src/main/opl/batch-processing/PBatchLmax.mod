/*********************************************
 * OPL 12.3 Model
 * Author: Arnaud Malapert
 * Creation Date: 16 déc. 2011 at 15:28:03
 *********************************************/

include "batch-processing-common.mod";

dvar int+ DueDates[Batches];

//int MinDueDate = min( i in Jobs) JobData[i].dueDate;

subject to {
  forall( i in Jobs ) 
   forall( k in Batches ) 
     ctDueDates:
      	MaxDueDate * ( 1 - Takes[i][k]) + JobData[i].dueDate >= DueDates[k] ;

 forall( k in 2..NbJobs ) 
     ctRuleEDD:
      	DueDates[k-1] <= DueDates[k] ;

  forall( k in Batches ) 
     ctLateness:
      	CompletionTimes[k] - DueDates[k] <= obj;
}      

	