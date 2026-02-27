COMMIT FORMATTING:

# for "commit message" please follow this format; (tag from the provided set+field+kanbanID)
a good example of a commit message: “fix rubber (#4)”.  fix is tag, rubber is the problem that was fixed, is the kanban task ID (#4)

- for "extended description" please copy and paste the following list of question and answer briefly:
“
- What did I change?
- Why did I change it?
- What files/classes I touched?
- Risks or anything unfinished?
- Test method(If been done): 
“

# a set of commit tags
- fix : bug fix
- feature : introduced a completely new feature 
- refactor : changed the existing features or behaviour
- test : tested, updated, added test cases
- docs :  comments, README, report notes, changed indescriptive names, formatting..

# connecting Kanban tasks to commits
simply use #ID of the task

# to commit from your terminal use:
git commit -m "fix rubber (#ID) \
-m "What did I change? " -m "Why did I change it?" -m “What files/classes I touched? “ -m “Risks or anything unfinished? “ -m “Test method (if been done): none”

# Tags
please use the tagging system v1.2.3 where vMAJOR.MINORPATCH
- major: breaking functionality chnage, new features, removed features
- minor: new versions of feature and iteration
- patch: bug fix


# 2AA4-Project-1

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=jeremicMina_2AA4-Project-1)
