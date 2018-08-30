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

## Execution Results
### Environment

Using AWS c5.xlarge instance type (4 CPU, 8GB RAM).

CloudHSM V2 (cavium): 2 HSM node(s)

CloudHSM Classic (luna): 1 HSM node(s)

All cryptographic key material used for signing was generated from the `secp256r1` curve.

Execution timing measures the max, min and mean of individual signing requests,
as well as total time to process all requests.


### Test 1 (CloudHSM V2)
The following data was collected running with the following command:

`bin/cloudhsm_eval cavium authenticator-1 1 2000`

This will execute 2000 signing requests with no concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~1%   | 86MB
cloudhsm_client | ~9%   | 32MB

#### Execution Timing
Max | Min | Mean | Total
--- | --- | ---- | ----
276 | 72  | 161  | 323,238

Mean signing request was 161 ms, with a total of 5.3 minutes required to complete.


### Test 2 (CloudHSM Classic)
The following data was collected running with the following command:

`bin/cloudhsm_eval luna authenticator 1 2000`

This will execute 2000 signing requests with no concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~5%   | 92MB
HsmClient? N/A  | N/A   | N/A

#### Execution Timing
Max | Min | Mean | Total
--- | --- | ---- | ----
49  | 15  | 16   | 33,471

Mean signing request was 16 ms, with a total of 33 seconds required to complete.


### Test 3 (CloudHSM V2)
The following data was collected running with the following command:

`bin/cloudhsm_eval cavium authenticator-1 10 2000`

This will execute 2000 signing requests with 10 threads of concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~3%   | 85MB
cloudhsm_client | ~65%  | 256MB

#### Execution Timing
Max | Min | Mean | Total
--- | --- | ---- | ----
240 | 72  | 106  | 21,465

Mean signing request was 106 ms, with a total of 21 seconds required to complete.

### Test 4 (CloudHSM Classic)
The following data was collected running with the following command:

`bin/cloudhsm_eval luna authenticator 10 2000`

This will execute 2000 signing requests with 10 threads of concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~9%   | 90MB
HsmClient? N/A  | N/A   | N/A

#### Execution Timing
Max | Min | Mean | Total
--- | --- | ---- | ----
267 | 38  | 64   | 12,898

Mean signing request was 64 ms, with a total of 13 seconds required to complete.

### Test 5 (CloudHSM V2)
The following data was collected running with the following command:

`bin/cloudhsm_eval cavium authenticator-1 100 2000`

This will execute 2000 signing requests with 100 threads of concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~3%   | 84MB
cloudhsm_client | ~66%  | 2.4GB

#### Execution Timing
Max  | Min | Mean | Total
---- | --- | ---- | ----
1506 | 616 | 1021 | 20,852

Mean signing request was 1 second, with a total of 21 seconds required to complete.


### Test 6 (CloudHSM Classic)
The following data was collected running with the following command:

`bin/cloudhsm_eval luna authenticator 100 2000`

This will execute 2000 signing requests with 100 threads of concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~20%  | 94MB
HsmClient? N/A  | N/A   | N/A

#### Execution Timing
Max | Min | Mean | Total
--- | --- | ---- | ----
408 | 240 | 317  | 6,471

Mean signing request was 317 ms, with a total of 6 seconds required to complete.


### Test 7 (CloudHSM V2)
The following data was collected running with the following command:

`bin/cloudhsm_eval cavium authenticator-1 200 2000`

This will execute 2000 signing requests with 200 threads of concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~4%   | 83MB
cloudhsm_client | ~80%  | 4.9GB

#### Execution Timing
Max  | Min  | Mean | Total
---- | ---- | ---- | ----
3259 | 1342 | 2036 | 20,966

Mean signing request was 2 seconds, with a total of 21 seconds required to complete.


### Test 8 (CloudHSM Classic)
The following data was collected running with the following command:

`bin/cloudhsm_eval luna authenticator 200 2000`

This will execute 2000 signing requests with 200 threads of concurrency.

#### Instance Resource Consumption
Process         | CPU   | Memory (res)
--------------- | ----- | -------------
java            | ~15%  | 94MB
HsmClient? N/A  | N/A   | N/A

#### Execution Timing
Max  | Min | Mean | Total
---- | --- | ---- | ----
1152 | 528 | 906  | 9,267

Mean signing request was 906 ms, with a total of 9 seconds required to complete.

## Conclusions

CloudHSM V2 performance is measurably lower than Classic performance, even with twice as many HSM modules.
It also consumes significant system resources as request concurrency scales, w/o improvement in performance (when measured above 10 threads concurrency).

