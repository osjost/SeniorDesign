import subprocess
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
import numpy as np
import pandas as pd  # Import pandas for handling Excel output

def measure_curl_time():
    start_time = time.time()

    result = subprocess.run([
        "curl", "-k",
        "-H", "Authorization: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InVzZXJ0ZXN0MTIiLCJ1c2VySWQiOjExfQ.PhPb5tGRFmDwLwIx3X8e6y5-zu7n5tEX3l7CWDqod_c",
        "https://ec2-54-193-162-215.us-west-1.compute.amazonaws.com:443/readings/11/1"
    ], stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    end_time = time.time()
    duration = end_time - start_time

    return duration

def run_in_parallel(num_requests):
    durations = []  # List to store all durations

    with ThreadPoolExecutor(max_workers=num_requests) as executor:
        future_to_request = {executor.submit(measure_curl_time): i for i in range(num_requests)}
        
        for future in as_completed(future_to_request):
            request_id = future_to_request[future]
            try:
                duration = future.result()
                durations.append(duration)  # Add duration to the list
                print(f"Request {request_id} completed in {duration} seconds.")
            except Exception as exc:
                print(f"Request {request_id} generated an exception: {exc}")

    # After all requests are processed, write durations and summary statistics to an Excel file
    if durations:
        # Creating a DataFrame
        df = pd.DataFrame(durations, columns=['Duration'])
        
        # Adding summary statistics as a new row in the same DataFrame
        summary_statistics = pd.DataFrame({
            'Duration': [
                np.min(durations),
                np.max(durations),
                np.mean(durations),
                np.std(durations)
            ]
        }, index=['Minimum', 'Maximum', 'Average', 'Standard Deviation'])

        # Writing DataFrame to an Excel file
        with pd.ExcelWriter('request_durations.xlsx', engine='openpyxl') as writer:
            df.to_excel(writer, sheet_name='Durations')
            summary_statistics.to_excel(writer, sheet_name='Summary Statistics')

        print("Durations and summary statistics have been written to 'request_durations.xlsx'.")

if __name__ == "__main__":
    num_requests = 1000  # Number of parallel requests
    run_in_parallel(num_requests)
