import uuid
import random
from datetime import datetime
from locust import HttpLocust, TaskSet, task


class PiTaskSet(TaskSet):

    def on_start(self):
        pass

    @task(1)
    def pi(self):
        self.client.get('/pi/{}'.format(random.randint(10, 50000)))


class PiLocust(HttpLocust):
    task_set = PiTaskSet
    min_wait = 5000
    max_wait = 15000