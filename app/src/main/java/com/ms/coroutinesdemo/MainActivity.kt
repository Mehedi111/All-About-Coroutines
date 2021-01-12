package com.ms.coroutinesdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity_CoTest"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //if main is cancel then all the other thread will be cancelled also
        //every coroutines has its own coroutinescope instance attached to it

        composingSuspendFunc()
        coroutineCancellation()
        coroutineBuilder()
        variousCoroutines()
        coroutineContextDemo()
        firstDemo()
        suspenseDemo()
        coroutineContextSwitching()
        runBlockingTest()
        coroutineJobs()
        coroutineAsyncAndAwait()
        coroutineScoping()
        coroutineFlow()
    }

    private fun coroutineFlow() {
        /**
         * flow has 3 component
         * flow{} - builder use to create flow asFlow() or flowOf()
         * emit(value) - transmit a value
         * collect(value) - receive the value
         *
         * In coroutine, a flow is a type that can emit multiple values sequentially, as opposed to suspend function
         * that is return only single value
         * we can use flow to receive live updates from db
         * @{link: https://developer.android.com/kotlin/flow }
         * @see <a href="https://developer.android.com/kotlin/flow">link</a>
         * @see [link](https://developer.android.com/kotlin/flow)
         *
         * flow cant cancel itself, we need to cancel coroutine to cancel flow
         */


        runBlocking {

               val job = launch {
                   //consumer or downstream
                   sendNumbers().buffer().collect {
                       delay(2000)
                       Log.d(TAG, "coroutineFlow: receive Number $it")
                   }
               }

            /*(1..100).asFlow()
                .map {
                    delay(1000)
                    it
                }
                .filter {
                    it % 2 == 1
                }
                .onEach { check(it != 9) }
                .catch { e ->
                    Log.d(TAG, "coroutineFlow: ${e.message}")
                }
                .onCompletion {
                    Log.d(TAG, "coroutineFlow: completed")
                }
                *//*.transform {
                    emit("emitting : $it")
                }*//*
                .collect {
                    Log.d(TAG, "coroutineFlow: $it")
                }*/

        }


    }

    /**
     * flow example
     */
    fun sendNumbers(): Flow<Int> {
        return flow<Int> {
            //producer or upstream
            val numbers = arrayOf(1, 2, 3, 4, 5, 6, 8, 3, 4, 5, 6, 2, 4, 5, 3, 2, 1)
            numbers.forEach {
                emit(it)
                delay(1000)
            }
        }
        /*return listOf(1,2,3,4,5,6,7,8,9,10).asFlow()*/
        /*return flowOf(1,2,3,4,5,6,7)*/
    }


    tailrec fun fib(n: Int, s: Int, e: Int): Int { // 3, 1, 0
        return if (n == 0) {
            e
        } else {
            fib(n - 1, s + e, s)
            // 2, 1, 1 = 1
            // 1, 2, 1 =
            // 0, 3, 2 =
        }
    }

    private fun varAgsTest(vararg b: String, a: Int, s: String = "j") {

    }

    private fun infixFun() {
        //infix function must have only one parameter
        //we can use infix func like sentense it use to increase readability
        // every infix function is an extension function but every extension funtion is not infix function
        val sqrt = 10 pow 30.0
    }

    private infix fun Int.pow(double: Double): Double {
        return Math.sqrt(double)
    }


    private fun varAgsTest(string: String, vararg ints: Int, ok: String) {

    }

    private fun testJobs() {
        val job = Job()
        CoroutineScope(Dispatchers.Main + job).launch {
            Log.d(TAG, "testJobs: started")
            delay(2000)
            Log.d(TAG, "testJobs: end")
        }

        job.cancel()

    }

    private fun composingSuspendFunc() {
        /*
        * 1. Sequential Execution
        * 2. Concurrent Execution
        * 3. Lazy coroutines Execution
        * */

        runBlocking {
            val one = msgOne()
            val two = msgTwo()
            Log.d(TAG, "composingSuspendFunc: $one $two")
            val x = async { msgOne() }
            Log.d(TAG, "composingSuspendFunc: ${x.await()}")

            val x2 = async(start = CoroutineStart.LAZY) { msgOne() }
            Log.d(TAG, "composingSuspendFunc: ${x2.await()}")
        }
    }

    private suspend fun msgOne(): String {
        delay(1000)
        Log.d(TAG, "msgOne: called msg one")
        return "Hello "
    }

    private suspend fun msgTwo(): String {
        delay(1000)
        return "World"
    }

    private fun coroutineCancellation() = runBlocking {
        Log.d(TAG, "coroutineCancellation: program started")
        val job: Job = launch {
            for (i in 1..500) {
                yield()
                Log.d(TAG, "coroutineCancellation: $i")
            }
        }

        delay(200)
        job.cancelAndJoin()
        /*job.cancel()
        job.join()*/
        Log.d(TAG, "coroutineCancellation: program ended")
    }

    private fun coroutineBuilder() {
        /*
        * launch, aynch, runblocking
        * */
        //launch {}
        //GlobalScope.launch{} is companion object
        // launch function creating coroutine in local scope
        // runBlocking use to test a function and its block the current thread
        runBlocking {
            Log.d(TAG, "coroutineBuilder: $this")
            val job: Job = launch {
                //coroutineScope.launch{} return a job
                Log.d(TAG, "coroutineBuilder: $this")
                Log.d(TAG, "coroutineBuilder: ${Thread.currentThread().name}")
            }


            val jobDeferred: Deferred<String> = async {
                //coroutineScope.launch{} return a job
                Log.d(TAG, "jobDeferred: $this")
                Log.d(TAG, "jobDeferred: ${Thread.currentThread().name}")

                "mehedi hasan"
            }

            job.join()
            //jobDeferred.join()
            // if we wanna use the return value of job deferred than user jobDeferred.await()
            val result = jobDeferred.await()
            Log.d(TAG, "coroutineBuilder: result: $result")
        }
    }

    private fun coroutineContextDemo() {
        //every coroutine has it own coroutine context
        //every coroutine scope is unique in every coroutine
        //but coroutines context can be inherited from parent

        /**
         * coroutine context has two component
         * @see Dispatchers decides on which thread the coroutine will execute
         * @see Job used to control coroutine
         *
         * we can assign a name to the coroutine
         */
        runBlocking {
            // main thread
            /**
             * @param this: Coroutine scope
             * @param coroutineContext: Coroutine context instance
             */
            // without parameter coroutine called CONFINED coroutine
            launch {
                //it inherit coroutine context from parent coroutine
                Log.d(TAG, "C1 : ${Thread.currentThread().name}")
            }

            // with parameter
            //its similar to GlobalScope.launch{} it top level declaration
            launch(Dispatchers.Default) {
                Log.d(TAG, "C2 : ${Thread.currentThread().name}")
                delay(3000)
                Log.d(TAG, "C2 after delay : ${Thread.currentThread().name}")
                // thread can be changed after suspend function according to thread availability

            }

            launch(Dispatchers.Unconfined) {
                //it will inherit context from the immediate parent
                // so it will run on main thread
                Log.d(TAG, "C4 : ${Thread.currentThread().name}")
                delay(300)
                Log.d(TAG, "C4 after delay : ${Thread.currentThread().name}")
                // it will run on some other thread after suspend func

                launch(coroutineContext) {
                    // it will run on the thread (c4 after delay)
                }

            }

            launch(coroutineContext) {
                //same as like as confined coroutine
            }
        }
    }

    private fun variousCoroutines() {
        runBlocking {
            //this is bloacking coroutins
            Log.d(TAG, "variousCoroutines: $this")

            launch {
                // it has standalone coroutine
                Log.d(TAG, "launch: $this")

                launch {
                    //its also has own standalone coroutine, that is different from parent
                    Log.d(TAG, "child_launch: $this")

                }
            }

            async {
                //it has deferred coroutine
                Log.d(TAG, "async: $this")

                async {
                    Log.d(TAG, "child_async: $this")

                }
            }
        }
    }

    private fun coroutineScoping() {
        findViewById<Button>(R.id.goNext).setOnClickListener {
            val job = CoroutineScope(Dispatchers.Main).launch {
                // don't use global scope in this situation, use lifecycler scope
                try {
                    while (true) {
                        delay(1000L)
                        Log.d(TAG, "coroutineScoping: still running")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "coroutineScoping: cancel ${e.message}")
                }
            }

            GlobalScope.launch {
                delay(3000L)
                Intent(this@MainActivity, SecondActivity::class.java).also {
                    job.cancelAndJoin()
                    startActivity(it)
                    finish()
                }
            }
        }
    }

    private fun coroutineAsyncAndAwait() {
        GlobalScope.launch(Dispatchers.IO) {
            val time = measureTimeMillis {
                Log.d(TAG, "coroutineAsyncAndAwait METHOD 1")

                val n1 = networkCallOne()
                val n2 = networkCallTwo()
                Log.d(TAG, "coroutineAsyncAndAwait 1: $n1")
                Log.d(TAG, "coroutineAsyncAndAwait 2: $n2")
            }

            val times2 = measureTimeMillis {
                Log.d(TAG, "coroutineAsyncAndAwait METHOD 1")

                val n1 = async { networkCallOne() }
                val n2 = async { networkCallTwo() }

                Log.d(TAG, "coroutineAsyncAndAwait 1: ${n1.await()}")
                Log.d(TAG, "coroutineAsyncAndAwait 2: ${n2.await()}")
            }

            Log.d(TAG, "coroutineAsyncAndAwait: req times $time")
            Log.d(TAG, "coroutineAsyncAndAwait: req times $times2")
        }
    }

    private suspend fun networkCallOne(): String {
        delay(2000L)
        return "response form net call 1"
    }

    private suspend fun networkCallTwo(): String {
        delay(2000L)
        return "response form net call 2"
    }

    private fun fib(n: Int): Long {
        return if (n == 0) 0
        else if (n == 1) 1
        else fib(n - 1) + fib(n - 2)
    }

    private fun coroutineJobs() {

        val job = GlobalScope.launch(Dispatchers.Default) {
            /* repeat(5) {
                 Log.d(TAG, "repeating...")
                 delay(1000L)
             }*/
            withTimeout(3000L) {
                Log.d(TAG, "coroutineJobs: started long running task")
                for (i in 30..40) {
                    if (isActive) { // check is already cancelled or not
                        Log.d(TAG, "coroutineJobs: result i = $i : ${fib(i)}")
                    }
                }
            }

            Log.d(TAG, "coroutineJobs: ended long running task")

        }

        runBlocking {
            delay(2000L)
            job.cancel()
            //job.join()
            Log.d(TAG, "cancelled jobs")
        }
    }

    private fun runBlockingTest() {
        //run blocking
        runBlocking {
            //it will block main thread
            //its need when want to run suspend function in main thread and we dont need asynchronous operations
            Log.d(TAG, "before block")
            launch {
                Log.d(TAG, "inside 1: called")
                delay(1000L)
            }
            Log.d(TAG, "after 1 block")

            launch {
                Log.d(TAG, "inside 2: called")
                delay(1000L)
            }
            Log.d(TAG, "after 1 block")
            delay(2000L)
            Log.d(TAG, "end of scope")

        }
        Log.d(TAG, "end of program")

    }

    private fun coroutineContextSwitching() {
        // coroutines context
        //in general coroutine always started in a specific context
        GlobalScope.launch(Dispatchers.Unconfined) {
            val ans = doNetWorkCall()
            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.tvDummy).text = ans
            }
        }
    }

    private fun suspenseDemo() {
        //suspense
        GlobalScope.launch {
            delay(200L) //suspend function can only be executed inside coroutine or another suspense function

            val value = doNetWorkCall()
            Log.d(TAG, value)
            val value2 = doNetWorkCall()
            Log.d(TAG, value2)
        }
    }

    private fun firstDemo() {
        Log.d(TAG, "Normal: ${Thread.currentThread().name}")
        GlobalScope.launch {
            delay(3000L) //delay only pause the current coroutines not all the thread
            Log.d(TAG, "coroutine: ${Thread.currentThread().name}")
        }
    }

    suspend fun doNetWorkCall(): String {
        delay(3000L)
        return "Network called finished"
    }
}