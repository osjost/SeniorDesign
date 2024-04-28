import unittest
import requests

class TestAPICalls(unittest.TestCase):
    def setUp(self):
        # Disabling warnings for SSL Certificate for simplicity in testing.
        requests.packages.urllib3.disable_warnings()

    def test_get_readings(self):
        url = "https://ec2-54-193-162-215.us-west-1.compute.amazonaws.com:443/readings/7/1/0"
        headers = {
            "Authorization": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6Ik11ZWhlaGUiLCJ1c2VySWQiOjE3fQ.kCV-U-8gywVEApgrLOVu8oep7bX7uab4ALEmT3HmvaU"
        }
        response = requests.get(url, headers=headers, verify=False)
        self.assertEqual(response.status_code, 200)

    def test_post_associations(self):
        url = "https://ec2-54-193-162-215.us-west-1.compute.amazonaws.com:443/associations/"
        headers = {
            "Authorization": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InVzZXJ0ZXN0MTIiLCJ1c2VySWQiOjExfQ.PhPb5tGRFmDwLwIx3X8e6y5-zu7n5tEX3l7CWDqod_c",
            "Content-Type": "application/json"
        }
        data = '{"patient_id":"11", "provider_id":"1"}'
        response = requests.post(url, headers=headers, data=data, verify=False)
        self.assertEqual(response.status_code, 200)

    def test_delete_associations(self):
        url = "https://ec2-54-193-162-215.us-west-1.compute.amazonaws.com:443/associations/1"
        headers = {
            "Authorization": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InVzZXJ0ZXN0MTIiLCJ1c2VySWQiOjExfQ.PhPb5tGRFmDwLwIx3X8e6y5-zu7n5tEX3l7CWDqod_c"
        }
        response = requests.delete(url, headers=headers, verify=False)
        self.assertEqual(response.status_code, 200)

    # Add additional test methods for other cURL commands following the same pattern

if __name__ == '__main__':
    unittest.main()
