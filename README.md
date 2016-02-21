# Calculating prime numbers with Akka
This is a project from the Scala Pair Programming Breakfast - Budapest, which was held on [2016-02-09][1].
It takes a text file with a list of numbers, and calculates if the numbers are primes.
It uses the Akka Graph to split the work along multiple worker nodes

## TODO
- [ ] Write tests
- [ ] Make a partial graph from the file reader
- [ ] Solve the communication between the two partial graphs
- [ ] Make graph input, outputs generic
- [ ] Implement the same with Master-Worker actors

[1]: http://www.meetup.com/Scala-Pair-Programming-Breakfast-Budapest/events/228424508/
