package nextstep.subway.line.application;

import nextstep.subway.common.exception.LineNotFoundException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {
    private LineRepository lineRepository;
    private StationService stationService;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public LineResponse saveLine(LineRequest request) {
        Station upStation = stationService.findStation(request.getUpStationId());
        Station downStation = stationService.findStation(request.getDownStationId());
        Line persistLine = lineRepository.save(new Line(request.getName(), request.getColor(), upStation, downStation, request.getDistance(), request.getFee()));
        List<StationResponse> stations = StationResponse.of(persistLine.getStations());
        return LineResponse.of(persistLine, stations);
    }

    public List<LineResponse> findLines() {
        List<Line> persistLines = lineRepository.findAll();
        return persistLines.stream()
                .map(line -> LineResponse.of(line, StationResponse.of(line.getStations())))
                .collect(Collectors.toList());
    }

    public Line findLineById(Long id) {
        return lineRepository.findById(id).orElseThrow(() -> new LineNotFoundException("존재하지 않는 노선입니다."));
    }

    public LineResponse findLineResponseById(Long id) {
        Line persistLine = findLineById(id);
        List<StationResponse> stations = StationResponse.of(persistLine.getStations());
        return LineResponse.of(persistLine, stations);
    }

    public void updateLine(Long id, LineRequest lineUpdateRequest) {
        Line persistLine = lineRepository.findById(id).orElseThrow(() -> new LineNotFoundException("존재하지 않는 노선입니다."));
        persistLine.update(new Line(lineUpdateRequest.getName(), lineUpdateRequest.getColor()));
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    public void addLineStation(Long lineId, SectionRequest request) {
        Line line = findLineById(lineId);
        Station upStation = stationService.findStationById(request.getUpStationId());
        Station downStation = stationService.findStationById(request.getDownStationId());
        line.addLineStation(upStation, downStation, request.getDistance());
    }

    public void removeLineStation(Long lineId, Long stationId) {
        Line line = findLineById(lineId);
        Station station = stationService.findStationById(stationId);
        line.removeStation(station);
    }
}