var chart = c3.generate({
    bindto: '#window-slice-chart',
    size: {
      height: 200
    },
    data: {
        json: data1,
        type: 'bar',
        xFormat: '%H:%M',
        keys: {
            x: 'slice',
            value: ['NewOrderSingle', 'NewSlice']
        },
        groups: [['NewOrderSingle', 'NewSlice']]
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