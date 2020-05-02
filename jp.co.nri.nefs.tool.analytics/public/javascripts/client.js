var chart1 = c3.generate({
    bindto: '#window-count-by-slice-chart',
    size: {
      height: 200
    },
    data: {
        json: json1,
        type: 'bar',
        xFormat: '%H:%M',
        keys: {
            x: 'slice',
            value: ['NewOrderSingle', 'NewSlice', 'Other']
        },
        groups: [['NewOrderSingle', 'NewSlice', 'Other']]
    },
    axis: {
        x: {
            type: 'timeseries',
            tick: {
                format: '%H:%M'
            }
        }
    }
});
var chart2 = c3.generate({
    bindto: '#window-count-by-tradedate-chart',
    size: {
      height: 200
    },
    data: {
        json: json2,
        type: 'bar',
        xFormat: '%Y%m%d',
        keys: {
            x: 'trade_date',
            value: ['NewOrderSingle', 'NewSlice', 'Other']
        },
        groups: [['NewOrderSingle', 'NewSlice', 'Other']]
    },
    axis: {
        x: {
            type: 'timeseries',
            tick: {
                format: '%Y-%m-%d',
                culling: {
                    max: 5
                }
            }
        }
    }
});