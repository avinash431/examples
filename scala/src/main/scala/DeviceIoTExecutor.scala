package main.scala

import scala.collection.mutable.Map
import java.util.concurrent.{Executors}

/**
  * Created by jules on 12/30/15.
  * In this example, I use an Executor Service with a pool threads to generate devices information concurrently. Not only it's an illustration
  * of Scala expressive, succinct, and declarative way of programming, it shows the power of its functional programming.
  *
  * Note how Collection.foreach() methods are used in List collection to submit jobs, to iterate over each List of Maps, and to print each
  * device Map. You can't help but admire the tightness of its API.
  *
  * Simple few lines Scala.
  */
object DeviceIoTExecutor {

  /**
    * Using the foreach method of the List or Collection, print each device Map. In fact, this method
    * could be insert device data into an NoSQL or publish onto to a messaging system like Kafka.
    *
    * Also, note how filters are used from a previous Singleton instance, to create a a filtered
    * collection depending on the predicate
    * @param elem
    */
  def processMap(elem: List[scala.collection.mutable.Map[String, String]], thrName: String): Unit = {
    println("Processing each device map in the List generated by thread " + thrName)
    elem.foreach(println(_))
    //filter and create a new batch where the temperature is >= 30
    val filteredBatch: List[Map[String, String]] = elem.filter(TestIotDevice.filterByKey(_, "temp", 30))
    printf("%d Devices found where temperature is greater than or equal to %d\n", filteredBatch.length, 30)
    filteredBatch.foreach(println(_))
    //filter and create a new batch where the humidity is >=65
    val filteredBatch2: List[Map[String, String]] = elem.filter(TestIotDevice.filterByKey(_, "humidity", 65))
    printf("%d Devices found where humidity is greater than or equal to %d\n", filteredBatch2.length, 65)
    filteredBatch2.foreach(println(_))
  }

  def main(args:Array[String]): Unit = {

    val ndevices = args(0).toInt
    // for easy of creating equal batches, let's force the device number to be a multiple of three
    if (ndevices % 3 != 0) {
      println("Number of devices must be multiple of 3.")
      System.exit(1)
    }
    DeviceProvision.myPrint("Hello World! ")
    //create a pool of three threads, assuming we have three cores
    val cores = 3
    val pool = Executors.newFixedThreadPool(cores)
    val multiple = ndevices / 3
    var devGenerators: List[DeviceIoTGenerators] = List()
    //create list of three DeviceGenerator Runnable(s), each with its begin..end range of device numbers
    devGenerators = devGenerators.::(new DeviceIoTGenerators(1 until multiple))
    devGenerators = devGenerators.::(new DeviceIoTGenerators(multiple + 1 until 2 * multiple))
    devGenerators = devGenerators.::(new DeviceIoTGenerators((2 * multiple) + 1 until 3 * multiple))
    // Using foreach method on the list, submit each runnable to the executor service thread pool
    devGenerators.foreach(pool.submit(_))
    // let each Runnable finish in the poo; we could use an elaborate LatchCountDown if we wanted to , but since
    // this is a simple case, where the thread are unlikely to block or delay, a minor Sleep shall suffice.
    Thread.sleep(3000)
    // reverse the order of the List, since in Scala, for efficiency, Lists are appended to the front.
    devGenerators = devGenerators.reverse
    // Using foreach method, once again, iterate for each List[Map[String, String]] generated by each Runnable
    devGenerators.foreach(e => processMap (e.getDeviceBatches(), e.thrName ))
    println("Finished...")
  }

}
