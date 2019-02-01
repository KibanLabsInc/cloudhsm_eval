# CloudHSM V2 and Classic evaluation tool

This application is designed to evaluate processing time for given signing loads. (32 bytes, ECDSA)
## Usage

Uses the `application` gradle plugin to build a portable app.

### Build

To create the application, issue the following

`./gradlew installDist`

This creates the application under `build/install/cloudhsm_eval`

### Run

Execute the built application script found in the `bin` folder, called `cloudhsm_eval`.
It takes a number of arguments in the form of \<provider\> \<key alias\> \<concurrency\> \<count>


#### Example:

`bin/cloudhsm_eval cavium key1 10 1000`

The above uses the Cavium CloudHSM provider, and uses the private key identified by alias key1
to execute 1000 signing requests using a max concurrency of 10 threads.

Note: prompted Key Alias Credentials for luna is password only, while for cavium it expects username:password.

## Execution Results
### Environment

Using AWS c5.large instance type (2 CPU, 4GB RAM).

CloudHSM V2 (cavium): 6 HSM node(s)

CloudHSM Classic (luna): 1 HSM node(s)

All cryptographic key material used for signing was generated from the `secp256r1` curve.

Execution timing measures the max, min and mean of individual signing requests,
total time to process all requests and calculated calls per second.


### Test 1 (CloudHSM Classic)
```
$ bin/cloudhsm_eval luna authenticator 2 1000
Max: 21, Min: 6, Mean: 12, Total: 6252, Calls: 159.948816/s

$ bin/cloudhsm_eval luna authenticator 4 1000
Max: 25, Min: 6, Mean: 12, Total: 3057, Calls: 327.118090/s

$ bin/cloudhsm_eval luna authenticator 8 1000
Max: 28, Min: 6, Mean: 11, Total: 1521, Calls: 657.462196/s

$ bin/cloudhsm_eval luna authenticator 12 1000
Max: 24, Min: 6, Mean: 9, Total: 778, Calls: 1285.347044/s

$ bin/cloudhsm_eval luna authenticator 16 1000
Max: 24, Min: 6, Mean: 8, Total: 533, Calls: 1876.172608/s

$ bin/cloudhsm_eval luna authenticator 32 1000
Max: 46, Min: 8, Mean: 16, Total: 543, Calls: 1841.620626/s

$ bin/cloudhsm_eval luna authenticator 64 1000
Max: 81, Min: 19, Mean: 32, Total: 568, Calls: 1760.563380/s
```

These results indicate that the ideal concurrency would be around 16 threads, more than which shows a drop in performance.

Expected throughput of nearly 1900 signature operations per second.

### Test 2 (CloudHSM V2)
```
$ bin/cloudhsm_eval cavium authenticator-1 2 1000
Max: 20, Min: 7, Mean: 8, Total: 4394, Calls: 227.583068/s

$ bin/cloudhsm_eval cavium authenticator-1 8 1000
Max: 113, Min: 7, Mean: 17, Total: 2283, Calls: 438.020149/s

$ bin/cloudhsm_eval cavium authenticator-1 16 1000
Max: 124, Min: 7, Mean: 31, Total: 2039, Calls: 490.436488/s

$ bin/cloudhsm_eval cavium authenticator-1 32 1000
Max: 156, Min: 7, Mean: 51, Total: 1679, Calls: 595.592615/s

$ bin/cloudhsm_eval cavium authenticator-1 64 1000
Max: 208, Min: 8, Mean: 78, Total: 1243, Calls: 804.052424/s

$ bin/cloudhsm_eval cavium authenticator-1 92 1000
Max: 176, Min: 8, Mean: 79, Total: 980, Calls: 1092.145749/s

$ bin/cloudhsm_eval cavium authenticator-1 128 1000
Max: 263, Min: 8, Mean: 113, Total: 901, Calls: 1109.016303/s

$ bin/cloudhsm_eval cavium authenticator-1 192 1000
Max: 308, Min: 9, Mean: 153, Total: 815, Calls: 1225.790635/s

$ bin/cloudhsm_eval cavium authenticator-1 256 1000
Max: 404, Min: 9, Mean: 192, Total: 772, Calls: 1295.169020/s
```

These results indicate that the ideal concurrency would be between 128 and 192 threads, more than that seems to yield diminishing returns.

Expected throughput of nearly 1200 signature operations per second.