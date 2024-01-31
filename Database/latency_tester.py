import subprocess
import time

def measure_curl_time():
    # Start the timer
    start_time = time.time()

    # Execute the CURL command with the specified options and headers
    subprocess.run([
        "curl", "-k",
        "-H", "Authorization: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InVzZXJ0ZXN0MTIiLCJ1c2VySWQiOjExfQ.PhPb5tGRFmDwLwIx3X8e6y5-zu7n5tEX3l7CWDqod_c", 
        "https://ec2-54-193-162-215.us-west-1.compute.amazonaws.com:443/readings/11/1"
    ])

    # Stop the timer
    end_time = time.time()

    # Calculate the duration
    duration = end_time - start_time

    return duration

duration = measure_curl_time()
print(f"The CURL command took {duration} seconds to complete.")
