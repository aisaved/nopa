#Querying almanc patterns

# Response format

    {gl_percent_n: <value>
     sd_n: <value>
     accuracy_range_n: <value>
     symbol: <symbol>}

Where 'n' defines the number of past years. For example key 'sd_5' means Standard deviation for past 5 years of the gain loss percent

## Daily pattern

Example :

    /api/almanac/pattern?day=7&month=8&gl-range-start=1&gl-range-end=10&accuracy-range=70&sd=5&history-range-start=5&history-range-end=10&pattern-length=5&type=daily

## Parameters:

* type: daily (required)

* day: date day
 
* month: month
  
* gl-range-start: Gain loss range start
  
* gl-range-end: Gain loss range end
  
* accuracy-range: Minimum Percent win for pattern
  
* sd: Standard deviation of gain loss percentages
  
* history-range-start: past years start (Minimum 5)
  
* history-range-end: past years end (Maximum 100)
  
* pattern-length: number of days for pattern calculation


## Monthly pattern

Example :

    /api/almanac/pattern?month=8&gl-range-start=1&gl-range-end=10&accuracy-range=70&sd=5&history-range-start=5&history-range-end=10&type=monthly

## Parameters:

* type: monthly (required)
 
* month: month
  
* gl-range-start: Gain loss range start
  
* gl-range-end: Gain loss range end
  
* accuracy-range: Minimum Percent win for pattern
  
* sd: Standard deviation of gain loss percentages
  
* history-range-start: past years start (Minimum 5)
  
* history-range-end: past years end (Maximum 100)


## Weekly pattern

Example : 

    /api/almanac/pattern?week=8&gl-range-start=1&gl-range-end=10&accuracy-range=70&sd=5&history-range-start=5&history-range-end=10&type=weekly

## Parameters:

* type: weekly (required)
 
* week: week
  
* gl-range-start: Gain loss range start
  
* gl-range-end: Gain loss range end
  
* accuracy-range: Minimum Percent win for pattern
  
* sd: Standard deviation of gain loss percentages
  
* history-range-start: past years start (Minimum 5)
  
* history-range-end: past years end (Maximum 100)

### Month weekly

For querying n week of a month (1st week of January, 3rd week of September..etc)

Example :

    /api/almanac/pattern?week=8&gl-range-start=1&gl-range-end=10&accuracy-range=70&sd=5&history-range-start=5&history-range-end=10&type=weekly

### Parameters:

* type: weekly (required)
 
* week: week

* month: month
  
* gl-range-start: Gain loss range start
  
* gl-range-end: Gain loss range end
  
* accuracy-range: Minimum Percent win for pattern
  
* sd: Standard deviation of gain loss percentages
  
* history-range-start: past years start (Minimum 5)
  
* history-range-end: past years end (Maximum 100)


## Describe each pattern

For describing each pattern or to find the daily stats for each interval in a pattern length

Example:

    /api/almanac/pattern/describe?day=7&month=8&symbol=AAPL&pattern-length=20

This will return 20 days stats for symbol 'AAPL' for the pattern detected on 7th August