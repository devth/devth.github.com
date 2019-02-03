# Glucose and Insulin

When we eat foods that contain sugar or carbohydrates, our blood is flooded with
glucose. Insulin is then produced by the pancreas to shuttle the glucose out of
the blood, either into muscles and liver as glycogen or converted to
triglycerides by the liver if glycogen stores are already full.

The swings in blood glucose is called glycemic variability (GV). High GV is
associated with a ton of diseases like:

- metabolic disease
- Alzheimer's
- cancer
- arthritis
- heart disease (muscle cells turn into non-muscle fibers)

Corollary: Low GV is associated with increased longevity.

Increased glucose in the blood plasma causes a reaction from our pancreas. It
dumps insulin into the blood to shuttle the glucose into our muscle tissue and
liver to be stored as glycogen.

What happens if our muscles and liver glycogen stores are already full?

Insulin Resistance

##

In this post we'll attempt to build a model that can predict blood glucose
response and resulting insulin

## Interactive

Features:

- blood glucose data or use data from diabetic database
- tie into https://bloodcalculator.com/?
- ingested foods
  - checkbooxes with combos and photos of foods
    - meals from popular restaurants
    - a keto meal
    - a paleoo meal

> Blood glucose is predicted based on a patient's prior blood glucose levels,
> insulin data, meal data, exercise data, sleep patterns and work schedules

> To account for individual patient differences, separate models are trained for
> each patient.

Labels:

Predicated graph over a 12 hour window:

- glucose
- insulin

Correlated effects on longevity, risk of cancer,

How to take into account glycogen state of muscles and liver? Are those features
too?


## References

- [Glycemic Variability: How Do We Measure It and Why Is It Important?](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4543190/)
- [Machine Learning Models for Blood Glucose Prediction in Diabetes Management](http://smarthealth.cs.ohio.edu/shb.html)
- [Continuous Glucose Profiles in Healthy Subjects under Everyday Life Conditions and after Different Meals](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2769652/)
- [5 Simple Steps You Can Take To Live Longer, Banish Blood Sugar Swings & Massively Enhance Energy Levels.](https://bengreenfieldfitness.com/article/nutrition-articles/how-to-control-blood-sugar/)
