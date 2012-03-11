int NbJobs = ...;

range Jobs = 1..NbJobs;
range Batches = 1..NbJobs;

tuple jobData {
   int   duration;
   int   size;
   int 	 weight;
   int   dueDate;
 }
 
jobData JobData[Jobs] = ...;


//Single-machine 
int MachineCapacity = ...;

int MaxDueDate = max( i in Jobs) JobData[i].dueDate;
int SumDurations = sum( i in Jobs) JobData[i].duration;
 
dvar int obj;
dvar int Takes[Jobs][Batches] in 0..1;
dvar int+ Durations[Batches];
dvar int+ CompletionTimes[0..NbJobs];


execute PARAMS {
  //cplex.tilim = 3600*12; //12h
  cplex.tilim = 0.2
  //Resultats du tuning:
  cplex.brdir=1;
  cplex.cutsfactor=30;
  cplex.tuningtilim=1; //Sets a time limit per model and per test set
//  cplex.mipdisplay=1
}



minimize obj ;

  
subject to {
	
 forall( i in Jobs ) 
    ctPack:
      sum( k in Batches ) 
        Takes[i][k] == 1;

 forall( k in Batches ) 
    ctCapa:
      sum( i in Jobs ) 
        Takes[i][k] * JobData[i].size <= MachineCapacity;

  forall( i in Jobs ) 
   forall( k in Batches ) 
     ctDurations:
      	Durations[k] >= Takes[i][k] * JobData[i].duration;

  ctInitialTime:
    CompletionTimes[0] == 0;

  forall( k in Batches ) 
     ctSequencing:
      	CompletionTimes[k] == CompletionTimes[k-1] + Durations[k];



};

execute DISPLAY {
//2 	CPX_STAT_UNBOUNDED
//3 	CPX_STAT_INFEASIBLE
//4 	CPX_STAT_INForUNBD
//5 	CPX_STAT_OPTIMAL_INFEAS
//6 	CPX_STAT_NUM_BEST
//10 	CPX_STAT_ABORT_IT_LIM
//11 	CPX_STAT_ABORT_TIME_LIM
//12 	CPX_STAT_ABORT_OBJ_LIM
  var status = cplex.getCplexStatus();
  if( status == 1) {
    //1 CPX_OPTIMAL  Optimal solution found  
    writeln("s OPTIMAL")
  } else if ( status == 11){
    //11 	CPX_STAT_ABORT_TIME_LIM
    writeln("s SATISFIABLE");
  } else {
    writeln("s ERROR");
  }    
 //writeln("d RUNTIME " , ?);
 writeln("d OBJECTIVE " , obj);
 writeln("d LOWERBOUND " , cplex.getBestObjValue());
 writeln("d NODES ",cplex.getNnodes());
 var nbB = 0;
 var b;
 for( b in Batches) {
   if(Durations[b] > 0 ) nbB++;
 }
 writeln("d BATCHES ", nbB);
 writeln("d STATUS " , cplex.getCplexStatus());
 for( b in Batches) {
   write("s (ct=", CompletionTimes[b],", < ");
   for( j in Jobs) {
 	 	if(Takes[j][b] == 1) {
 		write(j," ");
 	} 		
 	}
 	writeln(">) ");
   }     
   
  
 
};
