int NbJobs = ...;
//int NbMachines = 1;

range Jobs = 1..NbJobs;
range Batches = 1..NbJobs;

tuple jobData {
   int   job;
   int   duration;
   int   size;
   int 	 weight;
   int   dueDate;
 }
 
jobData JobData[Jobs] = ...;

tuple machineData {
   int   machine;
   int   capacity;
}
//Single-machine 
machineData MachineData = ...;

int MaxDueDate = max( i in Jobs) JobData[i].dueDate;
int SumDurations = sum( i in Jobs) JobData[i].duration;
 
//int MaxValue = max(r in Resources) Capacity[r];


dvar int obj;
dvar int Takes[Jobs][Batches] in 0..1;
dvar int+ Durations[Batches];
dvar int+ CompletionTimes[0..NbJobs];


execute PARAMS {
  //cplex.tilim = 3600*12; //12h
  //cplex.tilim = 10
  //Resultats du tuning:
  //cplex.brdir=1;
  //cplex.cutsfactor=30;
  //cplex.tuningtilim=1; //Sets a time limit per model and per test set
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
        Takes[i][k] * JobData[i].size <= MachineData.capacity;

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
   if( cplex.getCplexStatus() == 1 ) {
    //1 CPX_OPTIMAL  Optimal solution found  
    writeln("s OPTIMAL ")
  } else {
    writeln("s SATISFIABLE ");
  }
 writeln("d LOWERBOUND " , cplex.getBestObjValue());
 	writeln("d NODES ",cplex.getNnodes());
 var nbB = 0;
 var b;
 for( b in Batches) {
   if(Durations[b] > 0 ) nbB++;
}
 writeln("d BATCHES ", nbB);
  writeln("\n\nSOLUTION:\nobj=",obj,"\nTakes=", Takes, "\nDurations=", Durations, 
 "\nCompletionTimes=", CompletionTimes);

};
