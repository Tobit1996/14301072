package test;

public class boss {
  private office office;
  private car car;
  
  @Autowired
  public boss(car car ,office office){
      this.car = car;
      this.office = office ;
  }

  public String toString(){
	  return "this boss has "+car.toString()+" and in"+office.toString();
  }
}
