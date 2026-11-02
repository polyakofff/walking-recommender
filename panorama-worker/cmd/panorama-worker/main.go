package main

import (
	"fmt"
)

func main() {
	// msgChan := make(chan string, 1)
	// close(msgChan)
	// stopChan := make(chan struct{})
	// close(stopChan)

	// select {
	// case msgChan <- "msg":
	// 	fmt.Println("msg sent")
	// case <-stopChan:
	// 	fmt.Println("stop signal received")
	// }

	// testChan := make(chan int, 2)
	// wg := sync.WaitGroup{}
	// wg.Add(1)
	// go func() {
	// 	for {
	// 		select {
	// 		case k, ok := <-testChan:
	// 			if !ok {
	// 				wg.Done()
	// 				return
	// 			}
	// 			fmt.Println(k)
	// 			time.Sleep(time.Millisecond / 2)
	// 		}
	// 	}
	// }()
	// for i := 0; i < 15; i++ {
	// 	select {
	// 	case testChan <- i:
	// 		// default:
	// 	}
	// }
	// close(testChan)
	// wg.Wait()

	// var s *string
	// fmt.Println(s == nil)

	// var i interface{}
	// fmt.Println(s == nil)

	// i = s
	// fmt.Println(i.(*string) == nil)

	// var ages map[string]int
	// fmt.Println(ages == nil)    // "true"
	// fmt.Println(len(ages) == 0) // "true"
	// fmt.Println(ages["alice"])  // "0"
	// ages["alice"] = 21          // panic

	sum := 0.0
	for i := 51; i <= 100; i++ {
		sum += 1 / float64(i)
	}
	fmt.Println(1 - sum)
}
