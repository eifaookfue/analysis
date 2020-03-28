var chart = c3.generate({
    bindto: '#window-slice-chart',
    size: {
      height: 200
    },
    data: {
        json: data1,
        xFormat: '%H:%M',
        keys: {
            x: 'slice',
            value: ['newordersingle_count', 'newslice_count']
        }
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